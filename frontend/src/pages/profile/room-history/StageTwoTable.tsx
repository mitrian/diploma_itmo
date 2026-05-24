import type { RoomHistoryStageTwoRowDTO } from '../../../api/types'
import { inclusionLabel } from './labels'
import '../../../styles/ProfilePage.css'

type StageTwoTableProps = {
  rows: RoomHistoryStageTwoRowDTO[]
  winnerId: number | null
}

export function StageTwoTable({ rows, winnerId }: StageTwoTableProps) {
  const allUserLogins = Array.from(
    new Set(rows.flatMap((r) => r.ranks.map((rk) => rk.userLogin))),
  ).sort()
  const userDisplayByLogin = new Map<string, string>()
  for (const r of rows) {
    for (const rk of r.ranks) {
      if (!userDisplayByLogin.has(rk.userLogin)) {
        userDisplayByLogin.set(rk.userLogin, rk.userDisplayName)
      }
    }
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table className="profile-room__stage-two-table">
        <thead>
          <tr>
            <th>Финалист</th>
            <th>Включён</th>
            {allUserLogins.map((login) => (
              <th key={login} title={`@${login}`}>
                {userDisplayByLogin.get(login) ?? login}
              </th>
            ))}
            <th>Сумма</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const ranksByLogin = new Map(row.ranks.map((rk) => [rk.userLogin, rk.rank]))
            const isWinner = winnerId !== null && winnerId === row.restaurantId
            return (
              <tr key={row.restaurantId} className={isWinner ? 'winner-row' : undefined}>
                <td>
                  <div className="profile-room__rest-name">{row.name ?? '—'}</div>
                  <div className="profile-room__rest-meta">
                    Поз. {row.stageOnePosition} · одобрений: {row.stageOneApprovalCount}
                  </div>
                </td>
                <td>
                  {inclusionLabel(row.includedBy) ?? '—'}
                </td>
                {allUserLogins.map((login) => (
                  <td key={login} className="profile-room__rank-cell">
                    {ranksByLogin.has(login) ? ranksByLogin.get(login) : '—'}
                  </td>
                ))}
                <td className="profile-room__sum-cell">{row.rankSum}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
