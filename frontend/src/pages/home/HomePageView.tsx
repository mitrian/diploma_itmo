import { Link } from 'react-router-dom'
import '../../styles/HomePage.css'

export function HomePageView() {
  return (
    <div className="home">
      <h1 className="home__title">Совместный выбор ресторана</h1>
      <p className="home__lead">
        Сервис помогает группе людей договориться, куда пойти поесть
      </p>
      <section className="home__rules" aria-labelledby="home-rules-heading">
        <h2 id="home-rules-heading" className="home__rules-title">
          Как это устроено
        </h2>
        <ol className="home__rules-list">
          <li>
            <strong>Комната.</strong> Организатор создаёт комнату с паролем и передаёт участникам код и пароль.
          </li>
          <li>
            <strong>Лобби.</strong> Участники отмечают готовность. Когда все готовы, владелец комнаты переводит
            процесс дальше.
          </li>
          <li>
            <strong>Фильтры.</strong> Задаётся геозона поиска и типы кухни — чтобы в выбор
            попали только подходящие заведения.
          </li>
          <li>
            <strong>Первый этап.</strong> Каждый свайпает карточки ресторанов, по итогам
            отбираются финалисты.
          </li>
          <li>
            <strong>Второй этап.</strong> Участники расставляют финалистов по приоритету. Учитывается коллективное
            ранжирование.
          </li>
          <li>
            <strong>Итог.</strong> Система подводит победителя сессии; историю прошлых комнат можно посмотреть в
            личном кабинете.
          </li>
        </ol>
      </section>
      <Link to="/room" className="home__cta">
        Перейти к комнате
      </Link>
    </div>
  )
}
