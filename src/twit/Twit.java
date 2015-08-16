/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import jarowinkler.JaroWinkler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Twit {

    /**
     * @param args the command line arguments
     */
    //inisialisasi data untuk menyambungkan ke database
//    private static String database = "jdbc:mysql://localhost/odz";
//    private static String username = "root";
//    private static String password = "";
    private static String database = "jdbc:mysql://localhost/open_data_zis";
    private static String username = "open_data_zis";
    private static String password = "open_data_zis";
    //inisialisasi SQL serta command untuk ke database
    private static String SQL;
    private static Connection con;
    private static Statement stm;
    private static ResultSet rs;
    //inisialisasi class JaroWinkler
    static JaroWinkler JW = new JaroWinkler();

    public static void onStatus(Status status, int kode) {
        Twitter tf = new TwitterFactory().getInstance();
        //kode 1 : Format benar
        if (kode == 1) {
            StatusUpdate st = new StatusUpdate("Hi @" + status.getUser().getScreenName() + ", terima kasih telah berpartisipasi dengan OpenDataZIS :D");
            st.inReplyToStatusId(status.getId());
            try {
                tf.updateStatus(st);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //kode 2 : Format salah
        } else if (kode == 2) { //kas
            StatusUpdate st = new StatusUpdate("Hi @" + status.getUser().getScreenName() + ", ma'af format Anda salah! Format Kas Masjid: @opendatazis *nama_masjid*alamat*pemasukan*pengeluaran*saldo* #kasmasjid");
            st.inReplyToStatusId(status.getId());
            try {
                tf.updateStatus(st);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (kode == 3) { //qurban
            StatusUpdate st = new StatusUpdate("Hi @" + status.getUser().getScreenName() + ", ma'af format Anda salah! Format Qurban Masjid: @opendatazis *nama_masjid*alamat*kambingX*sapiX* #qurbanmasjid");
            st.inReplyToStatusId(status.getId());
            try {
                tf.updateStatus(st);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (kode == 4) { //zakat
            StatusUpdate st = new StatusUpdate("Hi @" + status.getUser().getScreenName() + ", ma'af format Anda salah! Format Zakat: @opendatazis *nama_masjid*alamat*jml_zakat* #zakatmasjid");
            st.inReplyToStatusId(status.getId());
            try {
                tf.updateStatus(st);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (kode == 5) { //others
            StatusUpdate st = new StatusUpdate("Hi @" + status.getUser().getScreenName() + ", ma'af format Anda salah! Gunakan keyword kas, qurban atau zakat untuk format yang benar!");
            st.inReplyToStatusId(status.getId());
            try {
                tf.updateStatus(st);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            StatusUpdate st = new StatusUpdate("Hi @" + status.getUser().getScreenName() + ", ma'af format Anda salah!");
            st.inReplyToStatusId(status.getId());
            try {
                tf.updateStatus(st);
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void userMention() throws SQLException, ParseException {
        Twitter twitter = new TwitterFactory().getInstance();
        try {
            Long since_id = cek_last_mention();
            Paging paging = new Paging().sinceId(since_id);
            List<Status> statuses = twitter.getMentionsTimeline(paging);
            for (twitter4j.Status status : statuses) {
                long ID_TWEET = status.getId();
                String ID_TWITTER = status.getUser().getScreenName();
                int FOLLOWER = status.getUser().getFollowersCount();
                int FOLLOWING = status.getUser().getFriendsCount();
                String TWEET = status.getText().replace("'", "\\'");

                Calendar cal = Calendar.getInstance();
                String create = status.getCreatedAt().toString();
                Date date = format(create);
                cal.setTime(date);
                int TAHUN = cal.get(Calendar.YEAR);
                String BULAN = new SimpleDateFormat("MMMMM").format(date);
                String TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                int MINGGU = cek_minggu(create);
                int FREQ_UPD = 1;

                if (ID_TWITTER.equals("opendatazis")) { //reply akun sendiri
                    save_data_mentions(ID_TWEET, ID_TWITTER, TWEET, TIME);
                } else {
                    save_data_mentions(ID_TWEET, ID_TWITTER, TWEET, TIME);
                    save_user(ID_TWITTER, FOLLOWER, FOLLOWING, FREQ_UPD);
                    if (cek_regex(TWEET).equals("kas")) {
                        //masukkan hasil regex pada array
                        String[] hasil = Regex(TWEET, cek_regex(TWEET));
                        //ambil isi array
                        String MASJID = hasil[1];
                        String ALAMAT = hasil[2];
                        String PEMASUKAN = hasil[3];
                        String PENGELUARAN = hasil[4];
                        String SALDO = hasil[5];
                        String[] cek_alamat = cek_alamat(MASJID, ALAMAT);
                        add_freq_upd(ID_TWITTER, FOLLOWER, FOLLOWING, FREQ_UPD);
                        save_keuangan(ID_TWEET, cek_alamat[0], cek_alamat[1], PEMASUKAN, PENGELUARAN, SALDO, MINGGU, BULAN, TAHUN);
                        onStatus(status, 1);
                    } else if (cek_regex(TWEET).equals("qurban")) {
                        //masukkan hasil regex pada array
                        String[] hasil = Regex(TWEET, cek_regex(TWEET));
                        //ambil isi array
                        String MASJID = hasil[1];
                        String ALAMAT = hasil[2];
                        String PEMASUKAN = hasil[3];
                        String PENGELUARAN = hasil[4];
                        String SALDO = hasil[5];
                        String[] cek_alamat = cek_alamat(MASJID, ALAMAT);
                        add_freq_upd(ID_TWITTER, FOLLOWER, FOLLOWING, FREQ_UPD);
//                        save(ID_TWEET, ID_TWITTER, TWEET, cek_alamat[0], cek_alamat[1], PEMASUKAN, PENGELUARAN, SALDO, BULAN, TAHUN);
                        onStatus(status, 1);
                    } else if (cek_regex(TWEET).equals("zakat")) {
                        //masukkan hasil regex pada array
                        String[] hasil = Regex(TWEET, cek_regex(TWEET));
                        //ambil isi array
                        String MASJID = hasil[1];
                        String ALAMAT = hasil[2];
                        String PEMASUKAN = hasil[3];
                        String PENGELUARAN = hasil[4];
                        String SALDO = hasil[5];
                        String[] cek_alamat = cek_alamat(MASJID, ALAMAT);
                        add_freq_upd(ID_TWITTER, FOLLOWER, FOLLOWING, FREQ_UPD);
//                        save(ID_TWEET, ID_TWITTER, TWEET, cek_alamat[0], cek_alamat[1], PEMASUKAN, PENGELUARAN, SALDO, BULAN, TAHUN);
                        onStatus(status, 1);
                    } else {
                        String pola_kas = "(kas)";
                        String pola_qurban = "(qurban)";
                        String pola_zakat = "(zakat)";
                        Pattern kas = Pattern.compile(pola_kas);
                        Pattern qurban = Pattern.compile(pola_qurban);
                        Pattern zakat = Pattern.compile(pola_zakat);
                        Matcher hasil_kas = kas.matcher(TWEET);
                        Matcher hasil_qurban = qurban.matcher(TWEET);
                        Matcher hasil_zakat = zakat.matcher(TWEET);
                        if (hasil_kas.find()) {
                            onStatus(status, 2);
                        } else if (hasil_qurban.find()) {
                            onStatus(status, 3);
                        } else if (hasil_zakat.find()) {
                            onStatus(status, 4);
                        } else {
                            onStatus(status, 5);
                        }
                    }
                }

            }
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }
    }

    public boolean cek_replied(long ID_TWEET) throws SQLException {
        String q_rep = "SELECT * FROM temp_data WHERE TWIT_ID=" + ID_TWEET;
        connect();
        rs = stm.executeQuery(q_rep);
        if (rs.next()) {
            return true; //sudah di-reply
        } else {
            return false; //belum di-reply
        }
    }

    public Long cek_last_mention() throws SQLException {
        Long TWIT_ID = null;
        String q_mentions = "SELECT ID_TWEET FROM tweet ORDER BY TIME DESC LIMIT 1";
        connect();
        rs = stm.executeQuery(q_mentions);
        while (rs.next()) {
            TWIT_ID = rs.getLong("ID_TWEET");
            return TWIT_ID; //tweet terakhir
        }
        return TWIT_ID;
    }

    public void add_freq_upd(String ID_TWITTER, int FOLLOWER, int FOLLOWING, int FREQ_UPD) throws SQLException {
        FREQ_UPD = 1;
        String q_frequpd = "SELECT `FREQ_UPD` FROM `twitter_user` WHERE ID_TWITTER='" + ID_TWITTER + "'";
        connect();
        rs = stm.executeQuery(q_frequpd);
        if (rs.next()) {
            FREQ_UPD = rs.getInt("FREQ_UPD") + 1;
            String upd_user = "UPDATE `twitter_user` SET FOLLOWER='" + FOLLOWER + "', FOLLOWING='" + FOLLOWING + "', FREQ_UPD='" + FREQ_UPD + "' WHERE ID_TWITTER='" + ID_TWITTER + "'";
            stm.executeUpdate(upd_user);
        } else {
            String add_user = "INSERT INTO `twitter_user`(`ID_TWITTER`, `FOLLOWER`, `FOLLOWING`, `FREQ_UPD`) "
                    + "VALUES ('" + ID_TWITTER + "', '" + FOLLOWER + "', '" + FOLLOWING + "', '" + FREQ_UPD + "')";
            stm.executeUpdate(add_user);
        }
    }

    public static String cek_regex(String tweet) {
        // String to be scanned to find the pattern.
        // Format tweet kas masjid==> @opendatazis *nama_masjid*alamat*pemasukan*pengeluaran*saldo* #kasmasjid
        String pattern_kas = "^\\@opendatazis[ ]*\\*(.*)\\*(.*)\\*(.\\d*.)\\*(.\\d*.).\\*(.\\d*.)\\*[ ]*#kasmasjid";
        // Format tweet qurban masjid==> @opendatazis *nama_masjid*alamat*kambingX*sapiX* #qurbanmasjid
        String pattern_qurban = "^\\@opendatazis[ ]*\\*(.*)\\*(.*)\\*(\\d*)\\*(\\d*)\\*[ ]*#qurbanmasjid";
        // Format tweet zakat masjid==> @opendatazis *nama_masjid*alamat*jml_zakat* #zakatmasjid
        String pattern_zakat = "^\\@opendatazis[ ]*\\*(.*)\\*(.*)\\*(\\d*)\\*[ ]*#zakatmasjid";

        // Create a Pattern object
        Pattern r_kas = Pattern.compile(pattern_kas);
        Pattern r_qurban = Pattern.compile(pattern_qurban);
        Pattern r_zakat = Pattern.compile(pattern_zakat);

        // Now create matcher object.
        Matcher m_kas = r_kas.matcher(tweet);
        Matcher m_qurban = r_qurban.matcher(tweet);
        Matcher m_zakat = r_zakat.matcher(tweet);

        if (m_kas.find()) {
            return "kas";
        } else if (m_qurban.find()) {
            return "qurban";
        } else if (m_zakat.find()) {
            return "zakat";
        } else {
            return "error";
        }
    }

    public static String[] Regex(String tweet, String info) {
        // String to be scanned to find the pattern.
        // Format tweet kas masjid==> @opendatazis *nama_masjid*alamat*pemasukan*pengeluaran*saldo* #kasmasjid
        String pattern_kas = "^\\@opendatazis[ ]*\\*(.*)\\*(.*)\\*(.\\d*.)\\*(.\\d*.).\\*(.\\d*.)\\*[ ]*#kasmasjid";
        // Format tweet qurban masjid==> @opendatazis *nama_masjid*alamat*kambingX*sapiX* #qurbanmasjid
        String pattern_qurban = "^\\@opendatazis[ ]*\\*(.*)\\*(.*)\\*(\\d*)\\*(\\d*)\\*[ ]*#qurbanmasjid";
        // Format tweet zakat masjid==> @opendatazis *nama_masjid*alamat*jml_zakat* #zakatmasjid
        String pattern_zakat = "^\\@opendatazis[ ]*\\*(.*)\\*(.*)\\*(\\d*)\\*[ ]*#zakatmasjid";

        // Create a Pattern object
        Pattern r_kas = Pattern.compile(pattern_kas);
        Pattern r_qurban = Pattern.compile(pattern_qurban);
        Pattern r_zakat = Pattern.compile(pattern_zakat);

        // Now create matcher object.
        Matcher m_kas = r_kas.matcher(tweet);
        Matcher m_qurban = r_qurban.matcher(tweet);
        Matcher m_zakat = r_zakat.matcher(tweet);

        if (m_kas.find() && info.equals("kas")) {
            String[] a = {m_kas.group(0), m_kas.group(1), m_kas.group(2), m_kas.group(3), m_kas.group(4), m_kas.group(5)};
            return a;
        } else if (m_qurban.find() && info.equals("qurban")) {
            String[] a = {m_qurban.group(0), m_qurban.group(1), m_qurban.group(2), m_qurban.group(3), m_qurban.group(4)};
            return a;
        } else if (m_zakat.find() && info.equals("zakat")) {
            String[] a = {m_zakat.group(0), m_zakat.group(1), m_zakat.group(2), m_zakat.group(3)};
            return a;
        } else {
            String[] a = {"error"};
            return a;
        }
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

    public void save_keuangan(long ID_TWEET, String MASJID, String ALAMAT, String PEMASUKAN, String PENGELUARAN, String SALDO, int MINGGU, String BULAN, int TAHUN) throws SQLException {
        connect();
        SQL = "INSERT INTO `tweet_keuangan`(`ID_TWEET`,`MASJID`,`ALAMAT`,`PEMASUKAN`, `PENGELUARAN`, `SALDO`, `MINGGU`, `BULAN`, `TAHUN`) "
                + "VALUES ('" + ID_TWEET + "','" + MASJID + "','" + ALAMAT + "','" + PEMASUKAN + "','" + PENGELUARAN + "','" + SALDO + "','" + MINGGU + "','" + BULAN + "','" + TAHUN + "')";
        stm.executeUpdate(SQL);
    }

    public void save_data_mentions(long ID_TWEET, String ID_TWITTER, String TWEET, String TIME) throws SQLException {
        connect();
        String SQL_mentions = "INSERT INTO `tweet`(`ID_TWEET`, `ID_TWITTER`, `TWEET`, `TIME`) VALUES ('" + ID_TWEET + "','" + ID_TWITTER + "','" + TWEET + "','" + TIME + "')";
        stm.executeUpdate(SQL_mentions);
    }

    public void save_user(String ID_TWITTER, int FOLLOWER, int FOLLOWING, int FREQ_UPD) throws SQLException {
        String q_savetweet = "SELECT `ID_TWITTER` FROM `twitter_user` WHERE ID_TWITTER='" + ID_TWITTER + "'";
        connect();
        FREQ_UPD = 0;
        rs = stm.executeQuery(q_savetweet);
        if (!rs.next()) {
            String add_user = "INSERT INTO `twitter_user`(`ID_TWITTER`, `FOLLOWER`, `FOLLOWING`, `FREQ_UPD`) "
                    + "VALUES ('" + ID_TWITTER + "', '" + FOLLOWER + "', '" + FOLLOWING + "', '" + FREQ_UPD + "')";
            stm.executeUpdate(add_user);
        }
    }

    public Date format(String create_date) throws ParseException {
        DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        inputFormat.setLenient(true);

        Date date = inputFormat.parse(create_date);
        return date;
    }

    public int cek_minggu(String create_date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        Date date = format(create_date);
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.FRIDAY);
        int MINGGU = 0;
        int DATE = cal.get(Calendar.DATE);
        String HARI = new SimpleDateFormat("EEEE").format(date);
        if (HARI.equals("Friday")) {
            int selisih = DATE - 7;
            if (selisih < 0) {
                MINGGU = 1;
            } else if (selisih > 0) {
                MINGGU = (int) (Math.floor(DATE / 7) + 1);
            } else {
                MINGGU = 1;
            }
        } else {
            Calendar last_friday = Calendar.getInstance();
            last_friday.setFirstDayOfWeek(Calendar.FRIDAY);
            last_friday.set(Calendar.DAY_OF_WEEK_IN_MONTH, last_friday.get(Calendar.WEEK_OF_MONTH) - 1);
            last_friday.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            MINGGU = (int) (Math.floor(last_friday.get(Calendar.DATE) / 7) + 1);
        }
        return MINGGU;
    }

    public String[] cek_alamat(String masjid, String alamat) throws SQLException {
        String[] cek_alamat = {"", ""};
        String[] hasilJW = {"", ""};
        if (!JW.cek_masjid(masjid).equals("false")) {
            String new_masjid = JW.cek_masjid(masjid);
            hasilJW = JW.cek_alamat(new_masjid, alamat);
            if (hasilJW[0].equals("false")) {
                cek_alamat[0] = masjid;
                cek_alamat[1] = alamat;
            } else {
                cek_alamat[0] = hasilJW[0];
                cek_alamat[1] = hasilJW[1];
            }
        } else {
            cek_alamat[0] = hasilJW[0];
            cek_alamat[1] = hasilJW[1];
        }
        return cek_alamat;
    }

    public void getLastFriday() {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        // reduce the "current" month by 1 to get the "previous" month
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, cal.get(Calendar.WEEK_OF_MONTH) - 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        System.out.println(cal.getTime());

        System.out.println(now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR));
    }

    public static void main(String[] args) throws SQLException, ParseException {
        Twit t = new Twit();
        t.userMention();
//        int a = t.cek_minggu("Tue Jun 23 13:15:10 ICT 2015");
//        System.out.println(a);
//        t.getLastFriday();

    }

}
