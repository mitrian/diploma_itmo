import type { RestaurantCardDTO } from '../../../api/types'

type StageOneRestaurantCardBodyProps = {
  card: RestaurantCardDTO
  omitName?: boolean
}

export function StageOneRestaurantCardBody({ card, omitName = false }: StageOneRestaurantCardBodyProps) {
  return (
    <>
      {!omitName && <h3 className="stage-one__name">{card.name}</h3>}
      <p className="stage-one__line">{card.address}</p>
      <p className="stage-one__line">
        <span className="stage-one__k">Время работы</span> {card.openingHours}
      </p>
      <p className="stage-one__line">
        <span className="stage-one__k">Телефон</span>{' '}
        <a href={`tel:${card.phone.replace(/\s/g, '')}`} className="stage-one__link">
          {card.phone}
        </a>
      </p>
      {card.websiteUrl ? (
        <p className="stage-one__line">
          <span className="stage-one__k">Сайт</span>{' '}
          <a href={card.websiteUrl} className="stage-one__link" target="_blank" rel="noopener noreferrer">
            {card.websiteUrl}
          </a>
        </p>
      ) : (
        <p className="stage-one__line stage-one__muted">Сайт не указан</p>
      )}
      <div className="stage-one__tags">
        <span className="stage-one__k">Кухни</span>
        <ul className="stage-one__tag-list">
          {card.kitchenTags.map((t) => (
            <li key={t.id} className="stage-one__tag">
              {t.labelRu}
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
