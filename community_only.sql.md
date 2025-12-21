-- Supabase SQL for Global Community (Only Missing Parts)
-- Run this to create Communities and Messages tables.
-- Profiles table handling is included but safe to run if exists.

-- =====================================================
-- 1. Profiles Table (SKIPPED - ALREADY EXISTS)
-- =====================================================
-- You already have a profiles table, so we skip creating it.

-- =====================================================
-- 2. Communities Table
-- =====================================================
-- Defines the different community groups (Global, Tech, etc.)

CREATE TABLE IF NOT EXISTS public.communities (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT, 
    member_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- RLS: Viewable by everyone.
ALTER TABLE public.communities ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Communities are viewable by everyone."
    ON public.communities
    FOR SELECT
    USING (true);

-- Insert Default Communities (6-7 items as requested)
INSERT INTO public.communities (name, description, icon_name)
VALUES 
    ('Global Community', 'Talk to everyone!', 'globe'),
    ('Early Birds', 'For the morning risers', 'sun'),
    ('Night Owls', 'Who stays up late?', 'moon'),
    ('Motivation', 'Get inspired', 'star'),
    ('Tech Talk', 'Discuss gadgets and code', 'code'),
    ('Wellness', 'Health and mindfulness', 'leaf'),
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
