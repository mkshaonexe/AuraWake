# Supabase SQL for Global Community

Copy and paste the following SQL into your Supabase SQL Editor to set up the database for the community features.

```sql
-- =====================================================
-- 1. Profiles Table
-- =====================================================
-- Stores user profile data like username and avatar.
-- Linked to auth.users.

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    username TEXT UNIQUE,
    full_name TEXT,
    avatar_url TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- RLS: Public profiles are viewable by everyone.
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public profiles are viewable by everyone."
    ON public.profiles
    FOR SELECT
    USING (true);

-- RLS: Users can insert their own profile.
CREATE POLICY "Users can insert their own profile."
    ON public.profiles
    FOR INSERT
    WITH CHECK (auth.uid() = id);

-- RLS: Users can update their own profile.
CREATE POLICY "Users can update own profile."
    ON public.profiles
    FOR UPDATE
    USING (auth.uid() = id);

-- =====================================================
-- 2. Communities Table
-- =====================================================
-- Defines the different community groups (Global, Tech, etc.)

CREATE TABLE IF NOT EXISTS public.communities (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT, -- e.g., 'public', 'code', 'palette' for mapping to icons in app
    member_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- RLS: Viewable by everyone.
ALTER TABLE public.communities ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Communities are viewable by everyone."
    ON public.communities
    FOR SELECT
    USING (true);

-- Insert Default Communities
INSERT INTO public.communities (name, description, icon_name)
VALUES 
    ('Global Community', 'Talk to everyone!', 'public'),
    ('Early Birds', 'For the mornining risers', 'wb_sunny'),
    ('Night Owls', 'Who stays up late?', 'bedtime'),
    ('Motivation', 'Get inspired', 'fitness_center'),
    ('Feature Requests', 'Feedback for AuraWake', 'lightbulb'),
    ('Support', 'Help and questions', 'help')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 3. Messages Table
-- =====================================================
-- Stores chat messages for each community.

CREATE TABLE IF NOT EXISTS public.messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    community_id UUID REFERENCES public.communities(id) ON DELETE CASCADE,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Policy: Everyone can read messages.
CREATE POLICY "Everyone can read messages"
    ON public.messages
    FOR SELECT
    USING (true);

-- Policy: Authenticated users can insert messages.
CREATE POLICY "Authenticated users can insert messages"
    ON public.messages
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- =====================================================
-- OPTIONAL: Functions & Triggers
-- =====================================================

-- Auto-create profile on signup (Optional, if you prefer DB side logic)
-- logic is complex to generating unique usernames here, so handled in App or Edge Function usually.
-- But a basic trigger to ensure a row exists is good.

```
