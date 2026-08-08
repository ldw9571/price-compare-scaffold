import React from 'react'

function FilterBar({ categories, filters, onChange }) {
  return (
    <div className="filter-bar">
      <select
        value={filters.category}
        onChange={(e) => onChange({ ...filters, category: e.target.value })}
      >
        <option value="">전체 카테고리</option>
        {categories.map((c) => (
          <option key={c} value={c}>{c}</option>
        ))}
      </select>

      <select
        value={filters.sort}
        onChange={(e) => onChange({ ...filters, sort: e.target.value })}
      >
        <option value="score">경쟁력 높은순</option>
        <option value="price">도매가 낮은순</option>
      </select>

      <label>
        최소 마진율(%)
        <input
          type="number"
          style={{ width: 60 }}
          value={filters.minMarginPercent}
          onChange={(e) => onChange({ ...filters, minMarginPercent: e.target.value })}
          placeholder="예: 20"
        />
      </label>

      <label>
        <input
          type="checkbox"
          checked={filters.moqOneOnly}
          onChange={(e) => onChange({ ...filters, moqOneOnly: e.target.checked })}
        />
        위탁 가능(MOQ=1)만
      </label>
    </div>
  )
}

export default FilterBar
