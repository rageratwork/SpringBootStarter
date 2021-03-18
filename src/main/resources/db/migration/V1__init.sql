-- init tables

DO $$
BEGIN

    IF NOT EXISTS ((SELECT * FROM information_schema.tables WHERE table_name = 'users')) THEN
        CREATE TABLE users (
            id character varying(36) PRIMARY KEY,
            username character varying(255) NOT NULL,
            email character varying(255),
            first_name character varying(255),
            last_name character varying(255),
            password character varying(60) NOT NULL,
            password_expired boolean DEFAULT false,
            enabled boolean NOT NULL DEFAULT true,
            locked boolean NOT NULL DEFAULT false,
            deleted boolean NOT NULL DEFAULT false,
            date_created timestamp without time zone NOT NULL DEFAULT now(),
            last_updated timestamp without time zone NOT NULL DEFAULT now(),
            version int NOT NULL DEFAULT 0
        );

        INSERT INTO users (id, username, first_name, password, password_expired) VALUES (
            uuid_generate_v4(),
            'admin',
            'Admin',
            '$2a$04$zuERn/tH4z3jrKXs0Baeg.jwogJPI5yY9f5o.WqL7ioh6R14g6Bq.',
            false
        );
    END IF;

    IF NOT EXISTS ((SELECT * FROM information_schema.tables WHERE table_name = 'roles')) THEN
        CREATE TABLE roles (
            id character varying(36) PRIMARY KEY,
            role character varying(255) NOT NULL,
            deleted boolean NOT NULL DEFAULT false,
            date_created timestamp without time zone NOT NULL DEFAULT now(),
            last_updated timestamp without time zone NOT NULL DEFAULT now(),
            version int NOT NULL DEFAULT 0
        );

        INSERT INTO roles (id, role) VALUES (uuid_generate_v4(), 'ROLE_ADMIN');
        INSERT INTO roles (id, role) VALUES (uuid_generate_v4(), 'ROLE_OWNER');
        INSERT INTO roles (id, role) VALUES (uuid_generate_v4(), 'ROLE_EDITOR');
        INSERT INTO roles (id, role) VALUES (uuid_generate_v4(), 'ROLE_MODERATOR');
        INSERT INTO roles (id, role) VALUES (uuid_generate_v4(), 'ROLE_USER');
        INSERT INTO roles (id, role) VALUES (uuid_generate_v4(), 'ROLE_GUEST');

        CREATE TABLE user_role (
            user_id character varying(36) NOT NULL,
            role_id character varying(36) NOT NULL,
            PRIMARY KEY(user_id, role_id)
        );

        ALTER TABLE user_role ADD CONSTRAINT fk_user_role_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
        ALTER TABLE user_role ADD CONSTRAINT fk_user_role_roles FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE;

        INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE role = 'ROLE_ADMIN'));

    END IF;
END;
$$