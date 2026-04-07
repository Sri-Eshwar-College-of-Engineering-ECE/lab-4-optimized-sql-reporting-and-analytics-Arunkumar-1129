package business;

/**
 * All analytical SQL queries using CTEs and Window Functions.
 */
public class BIReportQueries {

    // ----------------------------------------------------------------
    // 1. Monthly Sales Trend with MoM Growth (Window Function: LAG)
    // ----------------------------------------------------------------
    public static final String MONTHLY_SALES_TREND = """
        WITH monthly_sales AS (
            SELECT
                TO_CHAR(o.order_date, 'YYYY-MM')          AS month,
                SUM(oi.quantity * oi.unit_price)           AS total_revenue,
                COUNT(DISTINCT o.order_id)                 AS total_orders
            FROM orders o
            JOIN order_items oi ON o.order_id = oi.order_id
            WHERE o.status = 'Completed'
            GROUP BY TO_CHAR(o.order_date, 'YYYY-MM')
        )
        SELECT
            month,
            total_revenue,
            total_orders,
            LAG(total_revenue) OVER (ORDER BY month)       AS prev_month_revenue,
            ROUND(
                (total_revenue - LAG(total_revenue) OVER (ORDER BY month))
                / NULLIF(LAG(total_revenue) OVER (ORDER BY month), 0) * 100, 2
            )                                              AS mom_growth_pct
        FROM monthly_sales
        ORDER BY month;
        """;

    // ----------------------------------------------------------------
    // 2. Top-Performing Products by Revenue (Window: RANK + NTILE)
    // ----------------------------------------------------------------
    public static final String TOP_PRODUCTS = """
        WITH product_revenue AS (
            SELECT
                p.product_id,
                p.product_name,
                p.category,
                SUM(oi.quantity * oi.unit_price)  AS total_revenue,
                SUM(oi.quantity)                  AS units_sold
            FROM order_items oi
            JOIN products p    ON oi.product_id = p.product_id
            JOIN orders o      ON oi.order_id   = o.order_id
            WHERE o.status = 'Completed'
            GROUP BY p.product_id, p.product_name, p.category
        )
        SELECT
            product_name,
            category,
            total_revenue,
            units_sold,
            RANK()  OVER (ORDER BY total_revenue DESC)     AS revenue_rank,
            RANK()  OVER (PARTITION BY category ORDER BY total_revenue DESC) AS category_rank,
            NTILE(4) OVER (ORDER BY total_revenue DESC)    AS revenue_quartile
        FROM product_revenue
        ORDER BY revenue_rank;
        """;

    // ----------------------------------------------------------------
    // 3. Customer Behavior: RFM Analysis (Recency, Frequency, Monetary)
    // ----------------------------------------------------------------
    public static final String CUSTOMER_RFM = """
        WITH rfm_raw AS (
            SELECT
                c.customer_id,
                c.customer_name,
                c.segment,
                MAX(o.order_date)                          AS last_order_date,
                COUNT(DISTINCT o.order_id)                 AS frequency,
                SUM(oi.quantity * oi.unit_price)           AS monetary
            FROM customers c
            JOIN orders o      ON c.customer_id = o.customer_id
            JOIN order_items oi ON o.order_id   = oi.order_id
            WHERE o.status = 'Completed'
            GROUP BY c.customer_id, c.customer_name, c.segment
        ),
        rfm_scored AS (
            SELECT *,
                CURRENT_DATE - last_order_date             AS recency_days,
                NTILE(5) OVER (ORDER BY CURRENT_DATE - last_order_date)  AS r_score,
                NTILE(5) OVER (ORDER BY frequency)                       AS f_score,
                NTILE(5) OVER (ORDER BY monetary)                        AS m_score
            FROM rfm_raw
        )
        SELECT
            customer_name,
            segment,
            recency_days,
            frequency,
            ROUND(monetary::NUMERIC, 2)                    AS monetary,
            r_score, f_score, m_score,
            (r_score + f_score + m_score)                  AS rfm_total,
            CASE
                WHEN (r_score + f_score + m_score) >= 13 THEN 'Champion'
                WHEN (r_score + f_score + m_score) >= 10 THEN 'Loyal'
                WHEN (r_score + f_score + m_score) >= 7  THEN 'Potential'
                ELSE 'At Risk'
            END                                            AS customer_segment
        FROM rfm_scored
        ORDER BY rfm_total DESC;
        """;

    // ----------------------------------------------------------------
    // 4. Regional Sales Performance with Running Total (Window: SUM OVER)
    // ----------------------------------------------------------------
    public static final String REGIONAL_PERFORMANCE = """
        WITH region_monthly AS (
            SELECT
                c.region,
                TO_CHAR(o.order_date, 'YYYY-MM')          AS month,
                SUM(oi.quantity * oi.unit_price)           AS revenue
            FROM customers c
            JOIN orders o      ON c.customer_id = o.customer_id
            JOIN order_items oi ON o.order_id   = oi.order_id
            WHERE o.status = 'Completed'
            GROUP BY c.region, TO_CHAR(o.order_date, 'YYYY-MM')
        )
        SELECT
            region,
            month,
            revenue,
            SUM(revenue) OVER (PARTITION BY region ORDER BY month
                               ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total,
            ROUND(revenue / SUM(revenue) OVER (PARTITION BY month) * 100, 2)     AS region_share_pct
        FROM region_monthly
        ORDER BY region, month;
        """;

    // ----------------------------------------------------------------
    // 5. Product Category Contribution with Cumulative % (Window: SUM OVER)
    // ----------------------------------------------------------------
    public static final String CATEGORY_CONTRIBUTION = """
        WITH category_rev AS (
            SELECT
                p.category,
                SUM(oi.quantity * oi.unit_price)           AS revenue
            FROM order_items oi
            JOIN products p ON oi.product_id = p.product_id
            JOIN orders o   ON oi.order_id   = o.order_id
            WHERE o.status = 'Completed'
            GROUP BY p.category
        ),
        total AS (SELECT SUM(revenue) AS grand_total FROM category_rev)
        SELECT
            cr.category,
            cr.revenue,
            ROUND(cr.revenue / t.grand_total * 100, 2)    AS pct_of_total,
            ROUND(SUM(cr.revenue) OVER (ORDER BY cr.revenue DESC
                  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
                  / t.grand_total * 100, 2)                AS cumulative_pct
        FROM category_rev cr, total t
        ORDER BY cr.revenue DESC;
        """;
}
