import type { KitchenTagDTO } from '../../../api/types'

export type KitchenFiltersPanelProps = {
  catalog: KitchenTagDTO[] | null
  catalogLoading: boolean
  catalogError: string | null
  /** Объединение выборов всех участников (из API). */
  roomKitchenTags: KitchenTagDTO[]
  myKitchenTagSlugs: string[]
  kitchenFiltersLocked: boolean
  currentUserFiltersConfirmed: boolean
  pendingSlugs: string[]
  onPendingSlugsChange: (slugs: string[]) => void
  onAddSelected: () => void | Promise<void>
  onRemoveSlug: (slug: string) => void | Promise<void>
  onConfirmFilters: () => void | Promise<void>
  addSaving: boolean
  removeLoadingSlug: string | null
  confirmSaving: boolean
  actionError: string | null
  getTagLabel: (slug: string) => string
}

export function KitchenFiltersPanel({
  catalog,
  catalogLoading,
  catalogError,
  roomKitchenTags,
  myKitchenTagSlugs,
  kitchenFiltersLocked,
  currentUserFiltersConfirmed,
  pendingSlugs,
  onPendingSlugsChange,
  onAddSelected,
  onRemoveSlug,
  onConfirmFilters,
  addSaving,
  removeLoadingSlug,
  confirmSaving,
  actionError,
  getTagLabel,
}: KitchenFiltersPanelProps) {
  const claimedInRoom = new Set(roomKitchenTags.map((t) => t.slug))
  const available = catalog?.filter((t) => !claimedInRoom.has(t.slug)) ?? []

  const togglePendingSlug = (slug: string, checked: boolean) => {
    if (checked) {
      if (pendingSlugs.includes(slug)) return
      onPendingSlugsChange([...pendingSlugs, slug])
    } else {
      onPendingSlugsChange(pendingSlugs.filter((s) => s !== slug))
    }
  }

  return (
    <div className="room-detail__panel room-detail__panel--kitchen">
      <h2 className="room-detail__section-title">Типы кухни (фильтры)</h2>

      {kitchenFiltersLocked && (
        <p className="room-detail__kitchen-locked-msg">Все участники подтвердили фильтры — изменения недоступны.</p>
      )}

      <div className="room-detail__kitchen-room-summary">
        <h3 className="room-detail__subsection-title">Все типы кухни в комнате</h3>
        {roomKitchenTags.length === 0 ? (
          <p className="room-detail__hint">Пока никто не добавил типы кухни.</p>
        ) : (
          <ul className="room-detail__kitchen-chip-list room-detail__kitchen-chip-list--readonly">
            {roomKitchenTags.map((t) => (
              <li key={t.slug} className="room-detail__kitchen-chip room-detail__kitchen-chip--readonly">
                <span className="room-detail__kitchen-chip-label">{t.labelRu}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {catalogLoading && <p className="room-detail__loading-inline">Загрузка справочника…</p>}
      {catalogError && <p className="room-detail__ready-error">{catalogError}</p>}

      {!kitchenFiltersLocked && catalog && !catalogLoading && (
        <div className="room-detail__field room-detail__field--kitchen-select">
          <span className="room-detail__field-label">Добавить типы кухни</span>
          {available.length === 0 ? (
            <p className="room-detail__hint room-detail__hint--tight">Все типы из справочника уже закреплены в комнате.</p>
          ) : (
            <>
              <div className="room-detail__kitchen-pick-list" role="group" aria-label="Доступные типы кухни">
                {available.map((t) => (
                  <label key={t.slug} className="room-detail__kitchen-pick-item">
                    <input
                      type="checkbox"
                      className="room-detail__kitchen-pick-checkbox"
                      checked={pendingSlugs.includes(t.slug)}
                      disabled={addSaving}
                      onChange={(e) => togglePendingSlug(t.slug, e.target.checked)}
                    />
                    <span className="room-detail__kitchen-pick-label">{t.labelRu}</span>
                  </label>
                ))}
              </div>
              <button
                type="button"
                className="room-detail__btn room-detail__btn--secondary room-detail__btn--kitchen-add"
                disabled={addSaving || pendingSlugs.length === 0}
                onClick={() => void onAddSelected()}
              >
                {addSaving ? 'Добавление…' : 'Добавить выбранные'}
              </button>
            </>
          )}
        </div>
      )}

      <div className="room-detail__kitchen-chosen">
        <h3 className="room-detail__subsection-title">Мои выбранные типы</h3>
        {myKitchenTagSlugs.length === 0 ? (
          <p className="room-detail__hint">Пока ничего не выбрано.</p>
        ) : (
          <ul className="room-detail__kitchen-chip-list">
            {myKitchenTagSlugs.map((slug) => {
              const busy = removeLoadingSlug === slug
              return (
                <li key={slug} className="room-detail__kitchen-chip">
                  <span className="room-detail__kitchen-chip-label">{getTagLabel(slug)}</span>
                  {kitchenFiltersLocked ? null : (
                    <button
                      type="button"
                      className="room-detail__kitchen-chip-remove"
                      disabled={busy}
                      aria-label={`Удалить ${getTagLabel(slug)}`}
                      onClick={() => void onRemoveSlug(slug)}
                    >
                      {busy ? '…' : '×'}
                    </button>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </div>

      {!kitchenFiltersLocked && (
        <div className="room-detail__kitchen-confirm-block">
          {currentUserFiltersConfirmed ? (
            <p className="room-detail__hint">Вы подтвердили свой набор фильтров.</p>
          ) : (
            <button
              type="button"
              className="room-detail__btn room-detail__btn--primary"
              disabled={confirmSaving}
              onClick={() => void onConfirmFilters()}
            >
              {confirmSaving ? 'Отправка…' : 'Подтвердить мои фильтры'}
            </button>
          )}
        </div>
      )}

      {actionError && <p className="room-detail__ready-error">{actionError}</p>}
    </div>
  )
}
