package business;

public class BIReportMain {

    public static void main(String[] args) {
        BIReportService service = new BIReportService();

        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║         BUSINESS INTELLIGENCE REPORTING MODULE                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "trend"    -> service.monthlySalesTrend();
                case "products" -> service.topProducts();
                case "rfm"      -> service.customerRFM();
                case "regional" -> service.regionalPerformance();
                case "category" -> service.categoryContribution();
                default         -> { System.err.println("Unknown report: " + args[0]); printUsage(); }
            }
        } else {
            service.runAllReports();
        }
    }

    private static void printUsage() {
        System.out.println("""
            Usage: java BIReportMain [report]
              trend     - Monthly sales trend with MoM growth
              products  - Top-performing products ranking
              rfm       - Customer RFM segmentation
              regional  - Regional performance with running totals
              category  - Category contribution & cumulative %
              (no arg)  - Run all reports
            """);
    }
}
