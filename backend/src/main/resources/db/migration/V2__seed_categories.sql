-- ============================================================
--  Budgetix – Default System Categories Seed
-- ============================================================

-- Income Categories
INSERT INTO categories (id, name, icon, color, type, is_system) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Salary',          'pi-briefcase',   '#22C55E', 'INCOME',  TRUE),
  ('10000000-0000-0000-0000-000000000002', 'Freelance',        'pi-desktop',     '#10B981', 'INCOME',  TRUE),
  ('10000000-0000-0000-0000-000000000003', 'Investment',       'pi-chart-line',  '#06B6D4', 'INCOME',  TRUE),
  ('10000000-0000-0000-0000-000000000004', 'Business',         'pi-building',    '#8B5CF6', 'INCOME',  TRUE),
  ('10000000-0000-0000-0000-000000000005', 'Rental',           'pi-home',        '#F97316', 'INCOME',  TRUE),
  ('10000000-0000-0000-0000-000000000006', 'Gift',             'pi-gift',        '#A855F7', 'INCOME',  TRUE),
  ('10000000-0000-0000-0000-000000000007', 'Other Income',     'pi-plus-circle', '#6B7280', 'INCOME',  TRUE);

-- Expense – Parent Categories
INSERT INTO categories (id, name, icon, color, type, is_system) VALUES
  ('20000000-0000-0000-0000-000000000001', 'Food & Dining',    'pi-shopping-bag','#F97316', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000002', 'Transport',        'pi-car',         '#3B82F6', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000003', 'Housing',          'pi-home',        '#8B5CF6', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000004', 'Healthcare',       'pi-heart',       '#EC4899', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000005', 'Entertainment',    'pi-video',       '#F59E0B', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000006', 'Shopping',         'pi-shopping-cart','#EF4444','EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000007', 'Education',        'pi-book',        '#6366F1', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000008', 'Subscriptions',    'pi-sync',        '#14B8A6', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000009', 'Travel',           'pi-send',        '#F97316', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000010', 'Personal Care',    'pi-user',        '#EC4899', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000011', 'Utilities',        'pi-bolt',        '#EAB308', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000012', 'Savings Transfer', 'pi-wallet',      '#22C55E', 'EXPENSE', TRUE),
  ('20000000-0000-0000-0000-000000000013', 'Other Expense',    'pi-minus-circle','#6B7280', 'EXPENSE', TRUE);

-- Expense – Sub-categories: Food & Dining
INSERT INTO categories (id, parent_id, name, icon, color, type, is_system) VALUES
  ('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Groceries',    'pi-shopping-bag', '#F97316', 'EXPENSE', TRUE),
  ('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'Dining Out',   'pi-star',         '#F97316', 'EXPENSE', TRUE),
  ('21000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', 'Coffee',       'pi-sun',          '#F97316', 'EXPENSE', TRUE),
  ('21000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'Food Delivery','pi-box',          '#F97316', 'EXPENSE', TRUE);

-- Expense – Sub-categories: Transport
INSERT INTO categories (id, parent_id, name, icon, color, type, is_system) VALUES
  ('22000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'Fuel',         'pi-circle-fill',  '#3B82F6', 'EXPENSE', TRUE),
  ('22000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Public Transit','pi-map',         '#3B82F6', 'EXPENSE', TRUE),
  ('22000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', 'Taxi / Uber',  'pi-car',          '#3B82F6', 'EXPENSE', TRUE),
  ('22000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', 'Car Maintenance','pi-wrench',     '#3B82F6', 'EXPENSE', TRUE);

-- Expense – Sub-categories: Housing
INSERT INTO categories (id, parent_id, name, icon, color, type, is_system) VALUES
  ('23000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', 'Rent',         'pi-home',         '#8B5CF6', 'EXPENSE', TRUE),
  ('23000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', 'Mortgage',     'pi-building',     '#8B5CF6', 'EXPENSE', TRUE),
  ('23000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'Maintenance',  'pi-wrench',       '#8B5CF6', 'EXPENSE', TRUE),
  ('23000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000003', 'Furnishing',   'pi-table',        '#8B5CF6', 'EXPENSE', TRUE);

-- Expense – Sub-categories: Entertainment
INSERT INTO categories (id, parent_id, name, icon, color, type, is_system) VALUES
  ('25000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000005', 'Movies',       'pi-video',        '#F59E0B', 'EXPENSE', TRUE),
  ('25000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000005', 'Sports',       'pi-star',         '#F59E0B', 'EXPENSE', TRUE),
  ('25000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005', 'Games',        'pi-desktop',      '#F59E0B', 'EXPENSE', TRUE);

-- Expense – Sub-categories: Subscriptions
INSERT INTO categories (id, parent_id, name, icon, color, type, is_system) VALUES
  ('28000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000008', 'Streaming',    'pi-video',        '#14B8A6', 'EXPENSE', TRUE),
  ('28000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000008', 'Software',     'pi-desktop',      '#14B8A6', 'EXPENSE', TRUE),
  ('28000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000008', 'News / Magazines','pi-book',      '#14B8A6', 'EXPENSE', TRUE),
  ('28000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000008', 'Gym',          'pi-heart',        '#14B8A6', 'EXPENSE', TRUE);

-- Transfer Category
INSERT INTO categories (id, name, icon, color, type, is_system) VALUES
  ('30000000-0000-0000-0000-000000000001', 'Account Transfer', 'pi-arrows-h',   '#6B7280', 'TRANSFER', TRUE);

-- Default Auto-Categorization Rules
INSERT INTO auto_categorization_rules (category_id, keyword) VALUES
  ('20000000-0000-0000-0000-000000000002', 'uber'),
  ('20000000-0000-0000-0000-000000000002', 'lyft'),
  ('20000000-0000-0000-0000-000000000002', 'taxi'),
  ('21000000-0000-0000-0000-000000000003', 'starbucks'),
  ('21000000-0000-0000-0000-000000000003', 'coffee'),
  ('21000000-0000-0000-0000-000000000004', 'uber eats'),
  ('21000000-0000-0000-0000-000000000004', 'deliveroo'),
  ('21000000-0000-0000-0000-000000000004', 'doordash'),
  ('21000000-0000-0000-0000-000000000001', 'supermarket'),
  ('21000000-0000-0000-0000-000000000001', 'grocery'),
  ('21000000-0000-0000-0000-000000000001', 'walmart'),
  ('21000000-0000-0000-0000-000000000001', 'carrefour'),
  ('28000000-0000-0000-0000-000000000001', 'netflix'),
  ('28000000-0000-0000-0000-000000000001', 'spotify'),
  ('28000000-0000-0000-0000-000000000001', 'disney'),
  ('28000000-0000-0000-0000-000000000001', 'hulu'),
  ('28000000-0000-0000-0000-000000000002', 'adobe'),
  ('28000000-0000-0000-0000-000000000002', 'microsoft'),
  ('28000000-0000-0000-0000-000000000002', 'apple'),
  ('28000000-0000-0000-0000-000000000004', 'gym'),
  ('28000000-0000-0000-0000-000000000004', 'fitness'),
  ('20000000-0000-0000-0000-000000000011', 'electricity'),
  ('20000000-0000-0000-0000-000000000011', 'water bill'),
  ('20000000-0000-0000-0000-000000000011', 'internet'),
  ('20000000-0000-0000-0000-000000000011', 'phone bill'),
  ('10000000-0000-0000-0000-000000000001', 'salary'),
  ('10000000-0000-0000-0000-000000000001', 'payroll'),
  ('10000000-0000-0000-0000-000000000001', 'wage');
