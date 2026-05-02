import { test, expect } from '@playwright/test';

test('should load without console errors', async ({ page }) => {
  const errors: string[] = [];
  
  // Listen for console errors
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
    }
  });

  // Listen for unhandled exceptions
  page.on('pageerror', err => {
    errors.push(err.message);
  });

  // Navigate to the app
  await page.goto('/');

  // Wait for the app to initialize (look for the logo or a key element)
  await expect(page.getByText('ZXChange')).toBeVisible();

  // Wait for a few seconds to catch any delayed errors
  await page.waitForTimeout(3000);

  // Check if any errors were collected
  if (errors.length > 0) {
    console.error('Detected browser errors:', errors);
  }
  
  expect(errors).toHaveLength(0);
});

test('should display account data in TopBar', async ({ page }) => {
  await page.goto('/');
  
  // Wait for account data to load (should not be 'Loading...' or '---')
  const equityValue = page.locator('div:has-text("Equity") >> span').last();
  await expect(equityValue).not.toContainText('Loading...');
  await expect(equityValue).toContainText('$');
});
