-- V1__initial_schema.sql
-- Enabling PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create drones table with geospatial support
CREATE TABLE IF NOT EXISTS drones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(50) UNIQUE NOT NULL,
    device_name VARCHAR(255) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    manufacturer VARCHAR(100),

    -- Capabilities
    max_flight_time_minutes INTEGER,
    max_altitude_meters FLOAT,
    max_speed_ms FLOAT,
    max_payload_kg FLOAT,

    -- Location & Geo
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location GEOGRAPHY(POINT, 4326) GENERATED ALWAYS AS (ST_Point(longitude, latitude, 4326)::geography) STORED,

    -- Status & Battery
    status VARCHAR(50) NOT NULL DEFAULT 'IDLE',
    battery_percentage DOUBLE PRECISION NOT NULL,

    -- User & Metadata
    owner_user_id VARCHAR(255) NOT NULL,
    metadata JSONB,

    -- Timestamps
    last_heartbeat TIMESTAMP NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_drone_status ON drones(status);
CREATE INDEX IF NOT EXISTS idx_drone_owner ON drones(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_drone_heartbeat ON drones(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_drone_identifier ON drones(identifier);
CREATE INDEX IF NOT EXISTS idx_drone_location ON drones USING GIST(location);

-- Create audit logs table for Elasticsearch integration
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drone_id UUID REFERENCES drones(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    changed_by VARCHAR(255),
    changed_at TIMESTAMP,
    method_name VARCHAR(255),
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_drone ON audit_logs(drone_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(created_at DESC);
