/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jarowinkler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author idilhaq
 */
public class JaroWinkler {

//    //inisialisasi data untuk menyambungkan ke database
//    private static String database = "jdbc:mysql://localhost:3306/odz";
//    private static String username = "root";
//    private static String password = "";    
    private static String database = "jdbc:mysql://localhost/open_data_zis";
    private static String username = "open_data_zis";
    private static String password = "open_data_zis";
    //inisialisasi SQL serta command untuk ke database
    private static Connection con;
    private static Statement stm;
    private static ResultSet rs;

    public double compare(String s1, String s2) {
        return similarity(s1, s2);
    }

    public boolean isTokenized() {
        return true; // I guess?
    }

    /**
     * Returns normalized score, with 0.0 meaning no similarity at all, and 1.0
     * meaning full equality.
     */
    public static double similarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }

        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        // ensure that s1 is shorter than or same length as s2
        if (s1.length() > s2.length()) {
            String tmp = s2;
            s2 = s1;
            s1 = tmp;
        }

        // (1) find the number of characters the two strings have in common.
        // note that matching characters can only be half the length of the
        // longer string apart.
        int maxdist = s2.length() / 2;
        int c = 0; // count of common characters
        int t = 0; // count of transpositions
        int prevpos = -1;
        for (int ix = 0; ix < s1.length(); ix++) {
            char ch = s1.charAt(ix);

            // now try to find it in s2
            for (int ix2 = Math.max(0, ix - maxdist);
                    ix2 < Math.min(s2.length(), ix + maxdist);
                    ix2++) {
                if (ch == s2.charAt(ix2)) {
                    c++; // we found a common character
                    if (prevpos != -1 && ix2 < prevpos) {
                        t++; // moved back before earlier 
                    }
                    prevpos = ix2;
                    break;
                }
            }
        }

        // we don't divide t by 2 because as far as we can tell, the above
        // code counts transpositions directly.
        // System.out.println("c: " + c);
        // System.out.println("t: " + t);
        // System.out.println("c/m: " + (c / (double) s1.length()));
        // System.out.println("c/n: " + (c / (double) s2.length()));
        // System.out.println("(c-t)/c: " + ((c - t) / (double) c));
        // we might have to give up right here
        if (c == 0) {
            return 0.0;
        }

        // first compute the score
        double score = ((c / (double) s1.length())
                + (c / (double) s2.length())
                + ((c - t) / (double) c)) / 3.0;

        // (2) common prefix modification
        int p = 0; // length of prefix
        int last = Math.min(4, s1.length());
        for (; p < last && s1.charAt(p) == s2.charAt(p); p++);

        score = score + ((p * (1 - score)) / 10);

        // (3) longer string adjustment
        // I'm confused about this part. Winkler's original source code includes
        // it, and Yancey's 2005 paper describes it. However, Winkler's list of
        // test cases in his 2006 paper does not include this modification. So
        // is this part of Jaro-Winkler, or is it not? Hard to say.
        //
        //   if (s1.length() >= 5 && // both strings at least 5 characters long
        //       c - p >= 2 && // at least two common characters besides prefix
        //       c - p >= ((s1.length() - p) / 2)) // fairly rich in common chars
        //     {
        //     System.out.println("ADJUSTED!");
        //     score = score + ((1 - score) * ((c - (p + 1)) /
        //                                     ((double) ((s1.length() + s2.length())
        //                                                - (2 * (p - 1))))));
        // }
        // (4) similar characters adjustment
        // the same holds for this as for (3) above.
        return score;
    }

    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(database, username, password);
            stm = con.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String cek_masjid(String masjid) throws SQLException {
        connect();
        String SQL_cekMasjid = "SELECT NAMA FROM masjid";
        rs = stm.executeQuery(SQL_cekMasjid);
        double similarity = 0;
        String new_masjid = "";
        while (rs.next()) {
            double new_similarity = compare(masjid, rs.getString("NAMA"));
            if (new_similarity > similarity) {
                similarity = new_similarity;
                if (similarity > 0.8) {
                    new_masjid = rs.getString("NAMA");
                } else {
                    new_masjid = "false";
                }
            }
        }
        return new_masjid;
    }

    public String[] cek_alamat(String masjid, String alamat) throws SQLException {
//        String [] hasilJW;
        String[] hasil = new String[2];
        connect();
        String SQL_cekAlamat = "SELECT * FROM masjid WHERE NAMA='" + masjid + "'";
        rs = stm.executeQuery(SQL_cekAlamat);
        double similarity = 0;
        String new_alamat = "";
        while (rs.next()) {
            double new_similarity = compare(alamat, rs.getString("ALAMAT"));
            if (new_similarity > similarity) {
                similarity = new_similarity;
                if (similarity > 0.85) {
                    hasil[0] = masjid;
                    hasil[1] = rs.getString("ALAMAT");
                } else {
                    hasil[0] = "false";
                }
            }
        }
        return hasil;
    }

    public static void main(String[] args) throws SQLException {
        JaroWinkler jk = new JaroWinkler();
        String s1 = "its";
        String s2 = "Kampus ITS Keputih";
        double rjk = jk.compare(s1, s2);
        System.out.println("Jaro Winkler = "+rjk);
        
//        String masjid = "Manarul Ilmi";
//        String alamat = "Ds. Sekaran Lamongan";
//        String new_masjid = "";
//        String new_alamat = "";
//        if (!jk.cek_masjid(masjid).equals("false")) {
//            new_masjid = jk.cek_masjid(masjid);
//            String[] hasilJW;
//            hasilJW = jk.cek_alamat(new_masjid, alamat);
//            if (hasilJW[0].equals("false")) {
//                new_masjid = masjid;
//                new_alamat = alamat;
//            } else {
//                new_masjid = hasilJW[0];
//                new_alamat = hasilJW[1];
//            }
//        } else {
//            new_masjid = masjid;
//            new_alamat = alamat;
//        }
//        System.out.println(new_masjid + " - " + new_alamat);
    }
}
