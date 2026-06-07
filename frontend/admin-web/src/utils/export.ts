/**
 * 前端导入导出工具 — 兼容 xlsx 和 csv 格式
 * 不依赖后端导出接口，直接从表格数据生成文件
 */

// ═══ 导出 ═══

interface ExportColumn {
  key: string
  label: string
}

/**
 * 将数据导出为 CSV 或 XLSX 文件
 * - xlsx 格式实际生成 Excel 兼容的 XML Spreadsheet（无需第三方库）
 * - csv 格式使用标准 RFC 4180 编码
 */
export function exportFile(
  data: Record<string, any>[],
  columns: ExportColumn[],
  filename: string,
  format: 'xlsx' | 'csv' = 'xlsx',
) {
  if (format === 'csv') {
    downloadBlob(generateCSV(data, columns), `${filename}.csv`, 'text/csv;charset=utf-8')
  } else {
    downloadBlob(generateXLSX(data, columns), `${filename}.xlsx`, 'application/vnd.ms-excel')
  }
}

function generateCSV(data: Record<string, any>[], columns: ExportColumn[]): Blob {
  const BOM = '\uFEFF'
  const header = columns.map((c) => csvEscape(c.label)).join(',')
  const rows = data.map((row) =>
    columns.map((c) => csvEscape(String(row[c.key] ?? ''))).join(','),
  )
  return new Blob([BOM + header + '\n' + rows.join('\n')], { type: 'text/csv;charset=utf-8' })
}

function csvEscape(val: string): string {
  if (val.includes(',') || val.includes('"') || val.includes('\n')) {
    return '"' + val.replace(/"/g, '""') + '"'
  }
  return val
}

function generateXLSX(data: Record<string, any>[], columns: ExportColumn[]): Blob {
  const headerRow = columns.map((c) => `<Cell><Data ss:Type="String">${xmlEscape(c.label)}</Data></Cell>`).join('')
  const dataRows = data.map((row) =>
    '<Row>' +
    columns.map((c) => {
      const val = String(row[c.key] ?? '')
      const type = /^\d+(\.\d+)?$/.test(val) ? 'Number' : 'String'
      return `<Cell><Data ss:Type="${type}">${xmlEscape(val)}</Data></Cell>`
    }).join('') +
    '</Row>',
  )
  const xml = `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
  xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
  <Styles>
    <Style ss:ID="Header"><Font ss:Bold="1"/><Interior ss:Color="#F97316" ss:Pattern="Solid"/><Font ss:Color="#FFFFFF"/></Style>
  </Styles>
  <Worksheet ss:Name="Sheet1">
    <Table>
      <Row ss:StyleID="Header">${headerRow}</Row>
      ${dataRows.join('\n')}
    </Table>
  </Worksheet>
</Workbook>`
  return new Blob([xml], { type: 'application/vnd.ms-excel' })
}

function xmlEscape(s: string): string {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function downloadBlob(blob: Blob, filename: string, mimeType: string) {
  const url = URL.createObjectURL(new Blob([blob], { type: mimeType }))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// ═══ 导入 ═══

export interface ImportResult {
  headers: string[]
  rows: Record<string, string>[]
}

/**
 * 解析上传的 xlsx 或 csv 文件
 * - csv: 逐行解析，支持引号转义
 * - xlsx: 使用浏览器 DOMParser 解析 XML Spreadsheet
 */
export function parseImportFile(file: File): Promise<ImportResult> {
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (ext === 'csv') return parseCSV(file)
  if (ext === 'xlsx' || ext === 'xls') return parseXLSX(file)
  // 默认尝试 csv
  return parseCSV(file)
}

function parseCSV(file: File): Promise<ImportResult> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const text = String(reader.result).replace(/^\uFEFF/, '')
      const lines = text.split(/\r?\n/).filter((l) => l.trim())
      if (lines.length < 2) { reject(new Error('CSV 文件至少需要标题行和一行数据')); return }
      const headers = parseCSVLine(lines[0])
      const rows = lines.slice(1).map((line) => {
        const vals = parseCSVLine(line)
        const obj: Record<string, string> = {}
        headers.forEach((h, i) => { obj[h] = vals[i] ?? '' })
        return obj
      })
      resolve({ headers, rows })
    }
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsText(file, 'utf-8')
  })
}

function parseCSVLine(line: string): string[] {
  const result: string[] = []
  let current = ''
  let inQuotes = false
  for (let i = 0; i < line.length; i++) {
    const ch = line[i]
    if (inQuotes) {
      if (ch === '"' && line[i + 1] === '"') { current += '"'; i++ }
      else if (ch === '"') { inQuotes = false }
      else { current += ch }
    } else {
      if (ch === '"') { inQuotes = true }
      else if (ch === ',') { result.push(current.trim()); current = '' }
      else { current += ch }
    }
  }
  result.push(current.trim())
  return result
}

function parseXLSX(file: File): Promise<ImportResult> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      try {
        const parser = new DOMParser()
        const doc = parser.parseFromString(String(reader.result), 'text/xml')
        const rows = doc.getElementsByTagName('Row')
        if (rows.length < 2) { reject(new Error('XLSX 文件至少需要标题行和一行数据')); return }
        const getCells = (row: Element) => {
          const cells: string[] = []
          const dataEls = row.getElementsByTagName('Data')
          for (let i = 0; i < dataEls.length; i++) cells.push(dataEls[i].textContent?.trim() ?? '')
          return cells
        }
        const headers = getCells(rows[0])
        const dataRows: Record<string, string>[] = []
        for (let r = 1; r < rows.length; r++) {
          const vals = getCells(rows[r])
          const obj: Record<string, string> = {}
          headers.forEach((h, i) => { obj[h] = vals[i] ?? '' })
          dataRows.push(obj)
        }
        resolve({ headers, rows: dataRows })
      } catch (e) {
        reject(new Error('解析 XLSX 失败: ' + (e as Error).message))
      }
    }
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsText(file, 'utf-8')
  })
}
