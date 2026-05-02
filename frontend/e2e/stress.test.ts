import { test, expect } from '@playwright/test';

test('stability stress test: switch symbols every 30s for 2 minutes', async ({ page }) => {
  // Set a high timeout for the entire test (2 minutes + buffer)
  test.setTimeout(150000);

  const symbols = ['MSFT', 'GTX', 'INTL', 'AAPL'];
  
  page.on('console', msg => {
    console.log(`BROWSER [${msg.type()}]: ${msg.text()}`);
  });

  page.on('pageerror', err => {
    console.log(`BROWSER PAGE ERROR: ${err.message}`);
  });

  await page.goto('/');

  // Wait for initial load
  await expect(page.getByText('ZXChange')).toBeVisible();

  for (const symbol of symbols) {
    console.log(`\n>>> Step: Switching to ${symbol}`);

    // Find the symbol search input and type the symbol
    const searchInput = page.getByPlaceholder('Add symbol...');
    await searchInput.fill(symbol);
    await searchInput.press('Enter');

    // Give it a moment to appear in the watchlist
    await page.waitForTimeout(1000);

    // Click the symbol in the watchlist to select it
    const watchlistRow = page.locator('div.group').filter({ hasText: new RegExp(`^${symbol}$`) }).first();
    await watchlistRow.click();

    // Verify the chart header updates
    const chartHeader = page.locator('div').filter({ hasText: new RegExp(`^${symbol} —`) }).first();
    await expect(chartHeader).toBeVisible({ timeout: 15000 });

    // Verify that the chart rendered (at least one canvas element)
    const canvases = page.locator('canvas');
    await expect(canvases.first()).toBeVisible({ timeout: 20000 });
    const count = await canvases.count();
    console.log(`>>> Verification: ${symbol} chart rendered with ${count} canvas elements.`);

    // Wait for 30 seconds as requested
    console.log(`>>> Waiting 30s for ${symbol} to observe updates...`);
    await page.waitForTimeout(30000);
  }

  console.log('\n>>> Stress test completed successfully.');
});
