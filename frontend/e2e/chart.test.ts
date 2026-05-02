import { test, expect } from '@playwright/test';

test('chart should render canvas elements', async ({ page }) => {
  page.on('console', msg => console.log('BROWSER LOG:', msg.text()));
  page.on('pageerror', err => console.log('BROWSER ERROR:', err.message));
  
  await page.goto('/');

  // Wait for the app to load (heading)
  await expect(page.getByRole('heading', { name: 'Watchlists' })).toBeVisible();

  // Check if the chart panel is visible (default symbol AAPL)
  await expect(page.getByText('AAPL — 1Min — NASDAQ')).toBeVisible();

  // Wait for potential loading of bars
  await page.waitForTimeout(5000);

  // Check if loader is still there
  const loader = page.getByText('Loading Historical Data...');
  if (await loader.isVisible()) {
    console.log('Loader is still visible after 5s');
  }

  // Look for canvas elements within the chart container
  const canvases = page.locator('canvas');
  const count = await canvases.count();
  
  console.log(`Found ${count} canvas elements on page`);
  
  // Lightweight charts usually renders 3-4 canvases
  expect(count).toBeGreaterThan(0);
});
