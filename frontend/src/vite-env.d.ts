/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Интервал polling комнаты (мс), см. `src/config/polling.ts` */
  readonly VITE_ROOM_POLL_INTERVAL_MS?: string
}
