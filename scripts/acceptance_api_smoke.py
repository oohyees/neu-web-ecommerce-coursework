import json
import mimetypes
import os
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import base64
from pathlib import Path


BASE = os.environ.get("ACCEPTANCE_BASE_URL", "http://localhost:18080/api")
MAILHOG = os.environ.get("ACCEPTANCE_MAILHOG_URL", "http://localhost:18099")
RESULTS = []


def request(method, path, *, body=None, params=None, token=None, raw=False, files=None):
    url = BASE + path
    if params:
        url += "?" + urllib.parse.urlencode(params)
    headers = {}
    data = None
    if files:
        boundary = "----AcceptanceBoundary"
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        chunks = []
        for field, file_path in files.items():
            p = Path(file_path)
            mime = mimetypes.guess_type(p.name)[0] or "application/octet-stream"
            chunks.extend([
                f"--{boundary}\r\n".encode(),
                f'Content-Disposition: form-data; name="{field}"; filename="{p.name}"\r\n'.encode(),
                f"Content-Type: {mime}\r\n\r\n".encode(),
                p.read_bytes(),
                b"\r\n",
            ])
        chunks.append(f"--{boundary}--\r\n".encode())
        data = b"".join(chunks)
    elif body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body, ensure_ascii=False).encode()
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            payload = resp.read()
            if raw:
                return resp.status, payload, dict(resp.headers)
            return json.loads(payload.decode())
    except urllib.error.HTTPError as exc:
        payload = exc.read()
        if raw:
            return exc.code, payload, dict(exc.headers)
        try:
            return json.loads(payload.decode())
        except Exception:
            return {"success": False, "message": f"HTTP {exc.code}", "data": payload.decode(errors="ignore")}


def check(name, fn):
    try:
        detail = fn()
        RESULTS.append({"name": name, "status": "PASS", "detail": detail})
    except AssertionError as exc:
        RESULTS.append({"name": name, "status": "FAIL", "detail": str(exc)})
    except Exception as exc:
        RESULTS.append({"name": name, "status": "ERROR", "detail": repr(exc)})


state = {}


def ok(resp):
    assert resp.get("success") is True, resp
    return resp["data"]


def latest_mail_code(email):
    url = MAILHOG.rstrip("/") + "/api/v2/messages?limit=50"
    with urllib.request.urlopen(url, timeout=20) as resp:
        payload = json.loads(resp.read().decode())
    for item in payload.get("items", []):
        content = item.get("Content", {})
        headers = content.get("Headers", {})
        recipients = headers.get("To", [])
        body = content.get("Body", "")
        transfer_encoding = ",".join(headers.get("Content-Transfer-Encoding", [])).lower()
        if "base64" in transfer_encoding:
            body = base64.b64decode(body).decode("utf-8", errors="ignore")
        if any(email in r for r in recipients):
            import re
            match = re.search(r"(\d{6})", body)
            if match:
                return match.group(1)
    raise AssertionError(f"cannot find verification code email for {email}")


def auth_flow():
    user = ok(request("POST", "/auth/login", body={"username": "alice", "password": "123456"}))
    state["user_id"] = user["userId"]
    state["user_token"] = user["token"]
    admin = ok(request("POST", "/auth/admin/login", body={"username": "admin", "password": "admin123"}))
    state["admin_id"] = admin["adminId"]
    state["admin_token"] = admin["token"]
    return "user/admin login ok"


def registration_flow():
    suffix = str(int(time.time() * 1000))
    email = f"qa-{suffix}@example.com"
    ok(request("POST", "/auth/code", body={"email": email, "purpose": "REGISTER"}))
    code = latest_mail_code(email)
    data = ok(request("POST", "/auth/register/email", body={
        "username": f"qa_user_{suffix}",
        "password": "qa123456",
        "nickname": "QA",
        "email": email,
        "phone": "13811112222",
        "code": code,
    }))
    assert data["userId"] > 0
    state["qa_user_id"] = data["userId"]
    state["qa_email"] = email
    ok(request("POST", "/marketing/coupons/2/claim", params={"userId": data["userId"]}, token=state["user_token"]))
    claimed = ok(request("GET", f"/marketing/coupons/user/{data['userId']}", token=state["user_token"]))
    assert any(c["id"] == 2 for c in claimed)
    return "email registration ok"


def password_and_profile_flow():
    uid = state["user_id"]
    token = state["user_token"]
    profile = ok(request("GET", "/auth/profile", params={"userId": uid}, token=token))
    profile["nickname"] = "Alice QA"
    updated = ok(request("PUT", "/auth/profile", body=profile, token=token))
    assert updated["nickname"] == "Alice QA"
    ok(request("PUT", "/auth/password", body={
        "userId": uid, "oldPassword": "123456", "newPassword": "123456"
    }, token=token))
    reset_email = state.get("qa_email", updated["email"])
    ok(request("POST", "/auth/code", body={"email": reset_email, "purpose": "RESET"}))
    code = latest_mail_code(reset_email)
    ok(request("POST", "/auth/password/reset", body={
        "email": reset_email, "code": code, "password": "123456"
    }))
    return "profile/change/reset password ok"


def catalog_flow():
    cats = ok(request("GET", "/categories"))
    assert len(cats) >= 4
    page = ok(request("GET", "/products", params={"keyword": "keyboard", "sort": "price_asc", "page": 1, "size": 9}))
    assert page["total"] >= 1 and "keyboard" in page["items"][0]["name"].lower()
    filtered = ok(request("GET", "/products", params={"categoryId": 2, "page": 1, "size": 9}))
    assert filtered["total"] >= 2
    product = ok(request("GET", "/products/1"))
    assert "keyboard" in product["name"].lower()
    specs = ok(request("GET", "/marketing/specs/1"))
    assert len(specs) >= 1
    promotions = ok(request("GET", "/marketing/promotions"))
    assert any(p["promotionType"] == "FLASH_SALE" for p in promotions)
    coupons = ok(request("GET", "/marketing/coupons"))
    assert len(coupons) >= 2
    return "catalog/search/detail/spec/promotion/coupon ok"


def favorite_review_upload_flow():
    uid = state["user_id"]
    token = state["user_token"]
    ok(request("POST", "/favorites/1", params={"userId": uid}, token=token))
    assert ok(request("GET", "/favorites/1/status", params={"userId": uid}, token=token)) is True
    favorites = ok(request("GET", "/favorites", params={"userId": uid}, token=token))
    assert any(p["id"] == 1 for p in favorites)
    with tempfile.NamedTemporaryFile(delete=False, suffix=".png") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        temp_name = f.name
    try:
        uploaded = ok(request("POST", "/files/upload", token=token, files={"file": temp_name}))
    finally:
        os.unlink(temp_name)
    review = ok(request("POST", "/reviews", body={
        "userId": uid, "productId": 1, "rating": 5, "content": "QA review", "imageUrl": uploaded
    }, token=token))
    assert review["id"] > 0
    state["review_id"] = review["id"]
    reviews = ok(request("GET", "/reviews", params={"productId": 1}))
    assert any(r["content"] == "QA review" for r in reviews)
    ok(request("DELETE", "/favorites/1", params={"userId": uid}, token=token))
    return "favorite/review/upload ok"


def address_cart_order_flow():
    uid = state["user_id"]
    token = state["user_token"]
    address = ok(request("POST", "/addresses", body={
        "userId": uid, "receiverName": "QA", "phone": "13811112222",
        "province": "辽宁省", "city": "沈阳市", "district": "和平区",
        "detailAddress": "QA 路 1 号", "isDefault": True,
    }, token=token))
    ok(request("PUT", f"/addresses/{address['id']}/default", params={"userId": uid}, token=token))
    address["detailAddress"] = "QA 路 2 号"
    updated_address = ok(request("PUT", "/addresses", body=address, token=token))
    assert updated_address["detailAddress"] == "QA 路 2 号"
    addrs = ok(request("GET", "/addresses", params={"userId": uid}, token=token))
    assert any(a["id"] == address["id"] and a["isDefault"] for a in addrs), addrs
    request("DELETE", "/cart/items", params={
        "userId": uid, "productId": 1, "specText": "颜色:黑色 / 尺寸:87键"
    }, token=token)
    ok(request("POST", "/cart/items", body={"userId": uid, "productId": 1, "specText": "颜色:黑色 / 尺寸:87键", "quantity": 1}, token=token))
    cart = ok(request("GET", "/cart", params={"userId": uid}, token=token))
    assert any(i["productId"] == 1 and i["quantity"] >= 1 for i in cart), cart
    ok(request("PUT", "/cart/items", body={
        "userId": uid, "productId": 1, "specText": "颜色:黑色 / 尺寸:87键", "quantity": 2
    }, token=token))
    cart = ok(request("GET", "/cart", params={"userId": uid}, token=token))
    assert any(i["productId"] == 1 and i["quantity"] == 2 for i in cart), cart
    order_no = ok(request("POST", "/orders", body={
        "userId": uid, "addressId": address["id"], "productIds": [1], "couponId": None, "paymentMethod": "MOCK_PAY"
    }, token=token))["orderNo"]
    orders = ok(request("GET", "/orders", params={"userId": uid}, token=token))
    order = next(o for o in orders if o["orderNo"] == order_no)
    state["order_id"] = order["id"]
    assert order["paymentStatus"] == "UNPAID", order
    ok(request("PUT", f"/orders/{order['id']}/pay", token=token))
    ok(request("PUT", f"/orders/{order['id']}/refund", token=token))
    logistics = ok(request("GET", f"/orders/{order['id']}/logistics", token=token))
    assert any("已支付" in x["content"] for x in logistics), logistics
    detail = ok(request("GET", f"/orders/{order['id']}", token=token))
    assert detail["order"]["orderNo"] == order_no and len(detail["items"]) >= 1, detail
    ok(request("DELETE", "/cart/items", params={
        "userId": uid, "productId": 1, "specText": "颜色:黑色 / 尺寸:87键"
    }, token=token))
    ok(request("DELETE", f"/addresses/{address['id']}", token=token))
    return "address/cart/order/pay/refund/logistics ok"


def admin_flow():
    token = state["admin_token"]
    uid = state["user_id"]
    dashboard = ok(request("GET", "/admin/dashboard", token=token))
    assert "userCount" in dashboard and "salesTrend" in dashboard
    users = ok(request("GET", "/auth/admin/users", params={"keyword": "alice", "page": 1, "size": 10}, token=token))
    assert users["total"] >= 1
    ok(request("PUT", f"/auth/admin/users/{uid}/enabled", params={"enabled": True}, token=token))
    cat = ok(request("POST", "/admin/categories", body={"name": "QA分类", "sortOrder": 99}, token=token))
    ok(request("PUT", "/admin/categories", body={**cat, "name": "QA分类已改"}, token=token))
    prod = ok(request("POST", "/products/admin", body={
        "categoryId": 1, "name": "QA商品", "price": 9.9, "stock": 10, "sales": 0,
        "isOnSale": True, "imageUrl": "", "detailHtml": "", "paramsText": ""
    }, token=token))
    ok(request("PUT", "/products/admin", body={**prod, "stock": 11}, token=token))
    with tempfile.NamedTemporaryFile(delete=False, suffix=".csv", mode="w", encoding="utf-8") as f:
        f.write("categoryId,name,price,stock\n1,导入商品,19.9,3\n")
        temp_csv = f.name
    try:
        imported = ok(request("POST", "/products/admin/import", token=token, files={"file": temp_csv}))
    finally:
        os.unlink(temp_csv)
    assert imported["count"] == 1
    orders = ok(request("GET", "/admin/orders", params={"page": 1, "size": 10}, token=token))
    assert orders["total"] >= 1
    oid = state["order_id"]
    ok(request("PUT", f"/admin/orders/{oid}/ship", token=token))
    ok(request("PUT", f"/admin/orders/{oid}/refund/approve", token=token))
    ok(request("PUT", f"/admin/orders/{oid}/status", params={"status": "COMPLETED"}, token=token))
    ann = ok(request("POST", "/admin/announcements", body={"title": "QA公告", "content": "QA内容"}, token=token))
    ok(request("PUT", "/admin/announcements", body={**ann, "content": "QA内容2"}, token=token))
    feedback = ok(request("POST", "/feedback", body={"userId": uid, "content": "QA反馈"}, token=state["user_token"]))
    ok(request("PUT", "/admin/feedback", body={**feedback, "reply": "已收到"}, token=token))
    ok(request("PUT", f"/admin/feedback/{feedback['id']}/processed", token=token))
    banner = ok(request("POST", "/home/banners", body={
        "title": "QA轮播", "imageUrl": "/uploads/qa.png", "linkUrl": "/products", "sortOrder": 99
    }, token=token))
    ok(request("PUT", "/home/banners", body={**banner, "title": "QA轮播2"}, token=token))
    reviews = ok(request("GET", "/reviews/admin/all", params={"page": 1, "size": 10}, token=token))
    assert reviews["total"] >= 1
    ok(request("DELETE", f"/reviews/admin/{state['review_id']}", token=token))
    for path in ["/admin/dashboard/export", "/admin/orders/export", "/products/admin/export"]:
        status, payload, headers = request("GET", path, token=token, raw=True)
        assert status == 200 and len(payload) > 100 and "sheet" in headers.get("Content-Type", "")
    ok(request("DELETE", f"/home/banners/{banner['id']}", token=token))
    ok(request("DELETE", f"/admin/announcements/{ann['id']}", token=token))
    ok(request("DELETE", f"/products/admin/{prod['id']}", token=token))
    ok(request("DELETE", f"/admin/categories/{cat['id']}", token=token))
    return "admin CRUD/export ok"


def authz_flow():
    resp = request("GET", "/cart", params={"userId": 1})
    assert resp.get("success") is False and ("401" in resp.get("message","") or "未登录" in resp.get("message","") or "过期" in resp.get("message","")), resp
    user_resp = request("GET", "/admin/dashboard", token=state["user_token"])
    assert user_resp.get("success") is False and ("403" in user_resp.get("message","") or "权限不足" in user_resp.get("message","")), user_resp
    assert request("GET", "/products/admin/all").get("success") is False
    assert request("GET", "/reviews/admin/all").get("success") is False
    assert request("POST", "/home/banners", body={"title": "bad", "imageUrl": "", "linkUrl": "", "sortOrder": 0}).get("success") is False
    return "401/403 authz ok"


def main():
    checks = [
        ("认证登录", auth_flow),
        ("邮箱注册", registration_flow),
        ("资料与密码", password_and_profile_flow),
        ("商品目录", catalog_flow),
        ("收藏评价上传", favorite_review_upload_flow),
        ("地址购物车订单", address_cart_order_flow),
        ("后台管理", admin_flow),
        ("鉴权隔离", authz_flow),
    ]
    for name, fn in checks:
        check(name, fn)
    print(json.dumps(RESULTS, ensure_ascii=False, indent=2))
    failed = [r for r in RESULTS if r["status"] != "PASS"]
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
