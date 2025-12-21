-- =====================================================
-- App Version Management Table
-- =====================================================
-- This table stores version information for the AuraWake app
-- Used to notify users about new updates

CREATE TABLE IF NOT EXISTS app_versions (
    id SERIAL PRIMARY KEY,
    version_code INTEGER NOT NULL UNIQUE,
    version_name VARCHAR(20) NOT NULL,
    release_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    download_url TEXT,
    changelog TEXT NOT NULL,
    is_critical BOOLEAN DEFAULT FALSE,
    min_supported_version INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_app_versions_version_code ON app_versions(version_code DESC);

-- Enable Row Level Security
ALTER TABLE app_versions ENABLE ROW LEVEL SECURITY;

-- Policy: Anyone can read version info (for update checks)
CREATE POLICY "Anyone can read app versions"
    ON app_versions
    FOR SELECT
    USING (true);

-- Policy: Only authenticated admins can insert/update versions
-- Note: You'll need to create an admin role or use service role for this
CREATE POLICY "Only admins can insert versions"
    ON app_versions
    FOR INSERT
    WITH CHECK (false); -- Change to your admin check logic

CREATE POLICY "Only admins can update versions"
    ON app_versions
    FOR UPDATE
    USING (false); -- Change to your admin check logic

-- Insert initial version (current app version)
INSERT INTO app_versions (version_code, version_name, changelog, is_critical)
VALUES (
    1,
    '1.0',
    '🎉 Initial Release

• Smart alarm system with multiple wake-up challenges
• QR code, math, and shake challenges
• Adult content blocking feature
• Beautiful timeline-based UI
• Firebase analytics integration
• Dark mode support',
    false
)
ON CONFLICT (version_code) DO NOTHING;

-- Example: Insert a new version
-- INSERT INTO app_versions (version_code, version_name, changelog, is_critical, min_supported_version)
-- VALUES (
--     2,
--     '1.1.0',
--     '✨ New Features
-- 
-- • Added social features with friends
-- • Community page for global interactions
-- • Google Sign-In support
-- • Improved notification system
-- 
-- 🐛 Bug Fixes
-- 
-- • Fixed alarm not ringing issue
-- • Improved battery optimization handling
-- • UI improvements and polish',
--     false,
--     1
-- );

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update updated_at on row update
CREATE TRIGGER update_app_versions_updated_at
    BEFORE UPDATE ON app_versions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Update Analytics Table (Optional)
-- =====================================================
-- Track when users check for updates and install them

CREATE TABLE IF NOT EXISTS update_analytics (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    device_id TEXT,
    from_version_code INTEGER,
    to_version_code INTEGER,
    action VARCHAR(20) NOT NULL, -- 'checked', 'dismissed', 'updated'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_update_analytics_user_id ON update_analytics(user_id);
CREATE INDEX IF NOT EXISTS idx_update_analytics_created_at ON update_analytics(created_at DESC);

-- Enable RLS
ALTER TABLE update_analytics ENABLE ROW LEVEL SECURITY;

-- Policy: Users can insert their own analytics
CREATE POLICY "Users can insert their own update analytics"
    ON update_analytics
    FOR INSERT
    WITH CHECK (true);

-- Policy: Users can read their own analytics
CREATE POLICY "Users can read their own update analytics"
    ON update_analytics
    FOR SELECT
    USING (auth.uid() = user_id OR user_id IS NULL);
