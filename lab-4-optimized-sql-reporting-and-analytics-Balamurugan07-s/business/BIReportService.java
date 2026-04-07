package business;

import java.sql.*;

public class BIReportService {

    // ── Generic query runner ──────────────────────────────────────────
    private void runReport(String title, String sql) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  REPORT: " + title);
        System.out.println("=".repeat(70));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            // Print header
            StringBuilder header = new StringBuilder();
            int[] widths = new int[cols];
            for (int i = 1; i <= cols; i++) {
                widths[i - 1] = Math.max(meta.getColumnLabel(i).length(), 15);
                header.append(String.format("%-" + widths[i - 1] + "s  ", meta.getColumnLabel(i).toUpperCase()));
            }
            System.out.println(header);
            System.out.println("-".repeat(header.length()));

            // Print rows
            int rowCount = 0;
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    String val = rs.getString(i) == null ? "NULL" : rs.getString(i);
                    row.append(String.format("%-" + widths[i - 1] + "s  ", val));
                }
                System.out.println(row);
                rowCount++;
            }
            System.out.println("-".repeat(header.length()));
            System.out.printf("  %d row(s) returned%n", rowCount);

        } catch (SQLException e) {
            System.err.println("  [ERROR] " + e.getMessage());
        }
    }

    // ── Public report methods ─────────────────────────────────────────

    public void monthlySalesTrend() {
        runReport("Monthly Sales Trend with MoM Growth (%)", BIReportQueries.MONTHLY_SALES_TREND);
    }

    public void topProducts() {
        runReport("Top-Performing Products (Revenue Rank + Category Rank)", BIReportQueries.TOP_PRODUCTS);
    }

    public void customerRFM() {
        runReport("Customer RFM Analysis & Segmentation", BIReportQueries.CUSTOMER_RFM);
    }

    public void regionalPerformance() {
        runReport("Regional Sales Performance with Running Total", BIReportQueries.REGIONAL_PERFORMANCE);
    }

    public void categoryContribution() {
        runReport("Product Category Contribution & Cumulative %", BIReportQueries.CATEGORY_CONTRIBUTION);
    }

    public void runAllReports() {
        monthlySalesTrend();
        topProducts();
        customerRFM();
        regionalPerformance();
        categoryContribution();
    }
}
