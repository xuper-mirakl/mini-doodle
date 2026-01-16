-- If you keep this here, your DB user must have privileges to create extensions.
-- Otherwise run it once manually as postgres and remove/comment it here.
CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'slot_status') THEN
    CREATE TYPE slot_status AS ENUM ('AVAILABLE','BUSY');
  END IF;
END;
$$;

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS meetings (
  id UUID PRIMARY KEY,
  organizer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  start_ts TIMESTAMPTZ NOT NULL,
  end_ts TIMESTAMPTZ NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (end_ts > start_ts)
);

CREATE TABLE IF NOT EXISTS meeting_participants (
  meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  PRIMARY KEY (meeting_id, user_id)
);

CREATE TABLE IF NOT EXISTS time_slots (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  start_ts TIMESTAMPTZ NOT NULL,
  end_ts TIMESTAMPTZ NOT NULL,
  status slot_status NOT NULL,
  meeting_id UUID NULL REFERENCES meetings(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT NOT NULL DEFAULT 0,
  CHECK (end_ts > start_ts)
);

CREATE INDEX IF NOT EXISTS idx_time_slots_user_start ON time_slots (user_id, start_ts);
CREATE INDEX IF NOT EXISTS idx_time_slots_user_status_start ON time_slots (user_id, status, start_ts);
CREATE INDEX IF NOT EXISTS idx_time_slots_meeting ON time_slots (meeting_id);
