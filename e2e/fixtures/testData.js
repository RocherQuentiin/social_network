/**
 * Static E2E fixtures — aligned with E2eDataSeeder (Spring profile "e2e").
 * Password meets application rules: 8+ chars, upper, lower, digit, special.
 */
module.exports = {
  baseUrl: process.env.E2E_BASE_URL || 'http://localhost:8081',

  password: 'E2eTest!123',

  users: {
    student: {
      email: 'e2e.student@eleve.isep.fr',
      username: 'e2estudent',
      firstName: 'E2E',
      lastName: 'Student',
    },
    peer: {
      email: 'e2e.peer@eleve.isep.fr',
      username: 'e2epeer',
      firstName: 'E2E',
      lastName: 'Peer',
    },
    admin: {
      email: 'e2e.admin@isep.fr',
      username: 'e2eadmin',
      firstName: 'E2E',
      lastName: 'Admin',
    },
  },

  invalidLogin: {
    email: 'unknown.user@eleve.isep.fr',
    password: 'WrongPass!99',
  },

  register: {
    valid: {
      username: 'e2enewuser',
      email: 'e2e.newuser@eleve.isep.fr',
      firstName: 'New',
      lastName: 'Tester',
      password: 'E2eTest!123',
      bio: 'Nightwatch registration flow test account.',
    },
    invalidEmail: {
      username: 'badmail',
      email: 'not-an-isep-address@example.com',
      firstName: 'Bad',
      lastName: 'Email',
      password: 'E2eTest!123',
    },
    weakPassword: {
      username: 'weakpass',
      email: 'weak.pass@eleve.isep.fr',
      firstName: 'Weak',
      lastName: 'Pass',
      password: 'short',
    },
  },

  posts: {
    sampleContent: `E2E post ${Date.now()} — automated Nightwatch test.`,
  },

  projects: {
    name: `E2E Project ${Date.now()}`,
    description: 'Project created during Nightwatch E2E run.',
  },
};
