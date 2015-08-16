/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package levenshtein;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author idilhaq
 */
public class Levenshtein {
    //inisialisasi data untuk menyambungkan ke database
    private static String database = "jdbc:mysql://localhost:3306/opendatazis";
    private static String username = "root";
    private static String password = "";
    //inisialisasi SQL serta command untuk ke database
    private static Connection con;
    private static Statement stm;
    private static ResultSet rs;
    
    public static int distanceb(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public double compare(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());
        int dist = Math.min(distance(s1, s2), len);
        return 1.0 - (((double) dist) / ((double) len));
    }

    public static int distance(String s1, String s2) {
        if (s1.length() == 0) {
            return s2.length();
        }
        if (s2.length() == 0) {
            return s1.length();
        }

        int[][] matrix = new int[s1.length() + 1][s2.length() + 1];
        for (int col = 0; col <= s2.length(); col++) {
            matrix[0][col] = col;
        }
        for (int row = 0; row <= s1.length(); row++) {
            matrix[row][0] = row;
        }

        for (int ix1 = 0; ix1 < s1.length(); ix1++) {
            char ch1 = s1.charAt(ix1);
            for (int ix2 = 0; ix2 < s2.length(); ix2++) {
                int cost;
                if (ch1 == s2.charAt(ix2)) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                int left = matrix[ix1][ix2 + 1] + 1;
                int above = matrix[ix1 + 1][ix2] + 1;
                int aboveleft = matrix[ix1][ix2] + cost;
                matrix[ix1 + 1][ix2 + 1] = Math.min(left, Math.min(above, aboveleft));
            }
        }

        return matrix[s1.length()][s2.length()];
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
    
    public String cek_alamat(String alamat) throws SQLException{       
        connect();
        String SQL_cekMasjid = "SELECT ALAMAT FROM masjid";
        rs = stm.executeQuery(SQL_cekMasjid);
        double similarity = 0;
        String new_alamat = "";
        while(rs.next()) {            
            double new_similarity = compare(alamat, rs.getString("ALAMAT"));
            if(new_similarity > similarity){
                similarity = new_similarity;
                new_alamat = rs.getString("ALAMAT");
            }
        }
        return new_alamat+" = "+similarity;
    }

    public static void main(String[] args) throws SQLException {
//        String[] data = {"kitten", "sitting", "saturday", "sunday", "rosettacode", "raisethysword"};
//        for (int i = 0; i < data.length; i += 2) {
//            System.out.println("distance(" + data[i] + ", " + data[i + 1] + ") = " + distanceb(data[i], data[i + 1]));
//        }
        Levenshtein ls = new Levenshtein();
        double s = ls.compare("martha", "marhta");
        System.out.println(s);
//        System.out.println(ls.cek_alamat("Keputih ITS Surabaya"));
    }
}
