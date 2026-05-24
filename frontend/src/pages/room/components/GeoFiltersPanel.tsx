import {
  DEFAULT_CATALOG_CENTER_LAT,
  DEFAULT_CATALOG_CENTER_LON,
  DEFAULT_CATALOG_RADIUS_METERS,
} from '../../../config/catalogGeoDefaults'

export type GeoFiltersPanelProps = {
  currentUserIsOwner: boolean
  geoCenterLat: string
  geoCenterLon: string
  geoMaxDistanceMeters: string
  onGeoCenterLatChange: (value: string) => void
  onGeoCenterLonChange: (value: string) => void
  onGeoMaxDistanceMetersChange: (value: string) => void
  geoSaving: boolean
  geoError: string | null
  onSaveGeoFilter: () => void | Promise<void>
  geoConfirmSaving: boolean
  geoConfirmError: string | null
  onConfirmGeoFilter: () => void | Promise<void>
}

export function GeoFiltersPanel({
  currentUserIsOwner,
  geoCenterLat,
  geoCenterLon,
  geoMaxDistanceMeters,
  onGeoCenterLatChange,
  onGeoCenterLonChange,
  onGeoMaxDistanceMetersChange,
  geoSaving,
  geoError,
  onSaveGeoFilter,
  geoConfirmSaving,
  geoConfirmError,
  onConfirmGeoFilter,
}: GeoFiltersPanelProps) {
  return (
    <div className="room-detail__panel">
      <h2 className="room-detail__section-title">Настройка геофильтра</h2>
      {currentUserIsOwner ? (
        <>
          <p className="room-detail__hint">Задайте координаты центра и радиус, затем подтвердите геофильтр.</p>
          <div className="room-detail__form-grid">
            <label className="room-detail__field">
              <span className="room-detail__field-label">Широта</span>
              <input
                className="room-detail__input"
                type="number"
                step="any"
                value={geoCenterLat}
                onChange={(e) => onGeoCenterLatChange(e.target.value)}
                disabled={geoSaving || geoConfirmSaving}
                placeholder={DEFAULT_CATALOG_CENTER_LAT}
              />
            </label>
            <label className="room-detail__field">
              <span className="room-detail__field-label">Долгота</span>
              <input
                className="room-detail__input"
                type="number"
                step="any"
                value={geoCenterLon}
                onChange={(e) => onGeoCenterLonChange(e.target.value)}
                disabled={geoSaving || geoConfirmSaving}
                placeholder={DEFAULT_CATALOG_CENTER_LON}
              />
            </label>
            <label className="room-detail__field">
              <span className="room-detail__field-label">Радиус (метры)</span>
              <input
                className="room-detail__input"
                type="number"
                value={geoMaxDistanceMeters}
                onChange={(e) => onGeoMaxDistanceMetersChange(e.target.value)}
                disabled={geoSaving || geoConfirmSaving}
                placeholder={DEFAULT_CATALOG_RADIUS_METERS}
              />
            </label>
          </div>
          <div className="room-detail__ready-actions">
            <button
              type="button"
              className="room-detail__btn room-detail__btn--secondary"
              disabled={geoSaving || geoConfirmSaving}
              onClick={() => void onSaveGeoFilter()}
            >
              {geoSaving ? 'Сохранение…' : 'Сохранить геофильтр'}
            </button>
            <button
              type="button"
              className="room-detail__btn room-detail__btn--primary"
              disabled={geoSaving || geoConfirmSaving}
              onClick={() => void onConfirmGeoFilter()}
            >
              {geoConfirmSaving ? 'Подтверждение…' : 'Подтвердить геофильтр'}
            </button>
          </div>
          {geoError && <p className="room-detail__ready-error">{geoError}</p>}
          {geoConfirmError && <p className="room-detail__ready-error">{geoConfirmError}</p>}
        </>
      ) : (
        <p className="room-detail__hint">Ожидаем, пока владелец задаст и подтвердит геофильтр.</p>
      )}
    </div>
  )
}
