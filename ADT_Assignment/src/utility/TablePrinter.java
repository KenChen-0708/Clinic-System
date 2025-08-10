package utility;

public class TablePrinter {

    // Prints a table like:
    // +-----+-----------+
    // | Col | Col2      |
    // +-----+-----------+
    // | ... | ...       |
    // +-----+-----------+
    public static void printTable(String title, String[] headers, String[][] rows) {
        if (headers == null || rows == null) {
            System.out.println("(no data)");
            return;
        }
        int cols = headers.length;
        int[] widths = new int[cols];

        // 1) compute column widths
        for (int c = 0; c < cols; c++) {
            widths[c] = headers[c] != null ? headers[c].length() : 0;
        }
        for (int r = 0; r < rows.length; r++) {
            String[] row = rows[r];
            if (row == null) continue;
            for (int c = 0; c < cols; c++) {
                String cell = (c < row.length && row[c] != null) ? row[c] : "";
                if (cell.length() > widths[c]) widths[c] = cell.length();
            }
        }

        // 2) helpers
        String border = buildBorder(widths);
        System.out.println();
        if (title != null && !title.trim().isEmpty()) {
            System.out.println(title);
        }
        System.out.println(border);

        // 3) header row
        System.out.print("|");
        for (int c = 0; c < cols; c++) {
            System.out.print(" " + pad(headers[c], widths[c]) + " |");
        }
        System.out.println();
        System.out.println(border);

        // 4) data rows
        for (int r = 0; r < rows.length; r++) {
            String[] row = rows[r];
            System.out.print("|");
            for (int c = 0; c < cols; c++) {
                String cell = (row != null && c < row.length && row[c] != null) ? row[c] : "";
                System.out.print(" " + pad(cell, widths[c]) + " |");
            }
            System.out.println();
        }
        System.out.println(border);
        System.out.println();
    }

    private static String buildBorder(int[] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int w : widths) {
            for (int i = 0; i < w + 2; i++) sb.append("-");
            sb.append("+");
        }
        return sb.toString();
    }

    private static String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }
}
