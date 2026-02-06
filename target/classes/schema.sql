-- Blog Platform Database Schema
-- PostgreSQL 14+

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create ENUM types
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_banned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Index for faster lookups
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_banned ON users(is_banned);

-- =====================================================
-- POSTS TABLE
-- =====================================================
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_title_length CHECK (LENGTH(title) >= 1)
);

-- Indexes for performance
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_published ON posts(published);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_author_published ON posts(author_id, published);

-- =====================================================
-- COMMENTS TABLE
-- =====================================================
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) 
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT chk_content_length CHECK (LENGTH(content) >= 1)
);

-- Indexes for performance
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

-- =====================================================
-- TRIGGERS FOR UPDATED_AT TIMESTAMPS
-- =====================================================

-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for posts table
CREATE TRIGGER update_posts_updated_at 
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for comments table
CREATE TRIGGER update_comments_updated_at 
    BEFORE UPDATE ON comments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- INITIAL DATA (Optional)
-- =====================================================

-- Insert default admin user (password: 'admin123' - CHANGE IN PRODUCTION!)
-- Note: This is a BCrypt hash of 'admin123' with strength 10
INSERT INTO users (username, email, password, role, is_banned, created_at) 
VALUES (
    'admin',
    'admin@blogplatform.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    FALSE,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert sample user (password: 'user123' - CHANGE IN PRODUCTION!)
-- Note: This is a BCrypt hash of 'user123' with strength 10
INSERT INTO users (username, email, password, role, is_banned, created_at) 
VALUES (
    'testuser',
    'user@blogplatform.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'USER',
    FALSE,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert sample published post
INSERT INTO posts (title, content, author_id, published, created_at, updated_at)
SELECT 
    'Welcome to Our Blog Platform',
    'This is a sample blog post to demonstrate the platform capabilities. You can create, edit, and publish your own content!',
    u.id,
    TRUE,
    CURRENT_TIMESTAMP,
    NULL
FROM users u 
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- Insert sample draft post
INSERT INTO posts (title, content, author_id, published, created_at, updated_at)
SELECT 
    'Getting Started Guide',
    'This is a draft post that hasn''t been published yet. Learn how to use all the features of our blogging platform.',
    u.id,
    FALSE,
    CURRENT_TIMESTAMP,
    NULL
FROM users u 
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- Insert sample comment
INSERT INTO comments (content, author_id, post_id, created_at, updated_at)
SELECT 
    'Great post! Looking forward to more content.',
    u.id,
    p.id,
    CURRENT_TIMESTAMP,
    NULL
FROM users u 
CROSS JOIN posts p
WHERE u.username = 'testuser' 
AND p.title = 'Welcome to Our Blog Platform'
ON CONFLICT DO NOTHING;

-- =====================================================
-- USEFUL QUERIES FOR VERIFICATION
-- =====================================================

-- Query to verify the schema
COMMENT ON TABLE users IS 'Stores user accounts with authentication and role information';
COMMENT ON TABLE posts IS 'Stores blog posts created by users';
COMMENT ON TABLE comments IS 'Stores comments on blog posts';

-- Display table statistics
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
AND tablename IN ('users', 'posts', 'comments')
ORDER BY tablename;
