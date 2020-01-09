package windows.wsl.util;

import java.io.BufferedReader;
import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


/**
 * Seems not to be working as java.io.File#renameTo won't work across heterogeneous file systems.
 * Solutions can be to use java.nio classes as they work platform independently.
 * But its always better to use shell scripts for this kind of small utilities, development time will be 
 * much lesser and guaranteed to work reliably.
 */
public class Recycler {
    public static void main(final String[] args) throws Exception {
        final Recycler recycler = new Recycler();
        recycler.inspectOptions(args);
        recycler.process();
    }

    /* Constants */
    private final String APP_FOLDER_NAME = getClass().getSimpleName();
    private final String LOG_FILE_NAME = "log";
    private final File RECYCLE_BIN = new File(System.getProperty("java.io.tmpdir"), APP_FOLDER_NAME);
    private final File LOG_FILE = new File(RECYCLE_BIN, LOG_FILE_NAME);

    /* Flags -- default values set */
    private boolean verboseEnabled;
    private boolean doRecycle;
    private boolean doRestore;
    private boolean doFlush;
    private boolean showLog;

    private List<File> fileList = new ArrayList<>();

    public Recycler() throws IOException {        
        if(RECYCLE_BIN.exists()) {
            if(!RECYCLE_BIN.isDirectory()) {
                if(!RECYCLE_BIN.delete()) 
                    throw new IOException("Cannot delete file: " + RECYCLE_BIN.getAbsolutePath());
                if(!RECYCLE_BIN.mkdir()) 
                    throw new IOException("Cannot create App Directory: " + RECYCLE_BIN.getAbsolutePath());
            }
        } else
            if(!RECYCLE_BIN.mkdir())
                throw new IOException("Cannot create App Directory: " + RECYCLE_BIN.getAbsolutePath());
        if(!LOG_FILE.exists()) 
             if(!LOG_FILE.createNewFile()) 
                 throw new IOException("Cannot create log file: " + LOG_FILE.getAbsolutePath());
    }


    private void showHelp() { // TODO Complete
        System.out.println("Recycle bin folder location: " + RECYCLE_BIN);
        throw new UnsupportedOperationException("not designed yet");
    }
    
     private void showLog() throws IOException {         
         System.out.println("Application log information:");
         System.out.println("  File location: " + LOG_FILE);
         System.out.println("  Last accessed: " + new Date(LOG_FILE.lastModified()));
         if(LOG_FILE.length() == 0L) {
             System.out.println("No contents");
             return;
         }
         System.out.println("  Contents:");
         try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
             String line;
             while((line = reader.readLine()) != null)
                 System.out.println("  " + line);
         }
     }

    private void inspectOptions(final String[] commandLineArgs) {
        for(int i=0 , len=commandLineArgs.length; i<len; i++) {
            switch(commandLineArgs[i]) {
                case "-v":
                case "--verbose":
                    verboseEnabled = true;
                    break;

                case "-f":
                case "--flush":
                    doFlush = true;
                    return;

                 case "-l":
                 case "log":
                     showLog = true;
                     return;

                case "-r":
                case "--restore":
                    doRestore = true;
                    return;

                case "-h":
                case "--help":
                    showHelp();
                    System.exit(0);

                default:
                    fileList.add(new File(commandLineArgs[i]));
            }
        }

        if(fileList.size() == 0) {            
            showHelp();
            System.exit(1);
        }
        doRecycle = true;
    }

    private void doFlush() throws IOException {
        verbosePrint("Performing flush...");
        for(File file: RECYCLE_BIN.listFiles(f -> f.isDirectory()))
            delete(file);
        verbosePrint("Flush completed");
    }

    void delete(final File src) throws IOException {
        if(src.isDirectory())
            for(File file: src.listFiles())
                delete(file);
        if(!src.delete()) 
            throw new IOException("delete failed: " + src);
        verbosePrint("  deleted: %s", src);
    }


    private void restoreLastRecycledItem() throws IOException {
        File dir = getLatestDir();
        if(dir == null) {
            System.err.println("Err: Nothing to restore!");  
            return;
        }
        File currDir = new File(System.getProperty("user.dir"));
        verbosePrint("Restoring items of %s to %s...", new Date(Long.parseLong(dir.getName())), currDir);
        for(File file: dir.listFiles())
            move(file, currDir);
    }

    File getLatestDir() {
        File[] dirs = RECYCLE_BIN.listFiles(f -> f.isDirectory());
        if(dirs==null || dirs.length==0)
            return null;
        File latest = dirs[0];
        for(int i=1, len=dirs.length; i<len; i++) 
            if(dirs[i].lastModified() > latest.lastModified()) 
                latest = dirs[i];
        return latest;
    }


    private void process() throws IOException {
        if(doRestore) 
            restoreLastRecycledItem();
        else if(doRecycle) 
            doRecycle();
        else if(doFlush) 
            doFlush();
        else if(showLog) 
            showLog();
    }

    private void doRecycle() throws IOException {
        File dst = new File(RECYCLE_BIN, String.valueOf(new Date().getTime()));
        verbosePrint("Recycling to %s...", dst.getAbsolutePath());
        for(File file : fileList) {
            if(!file.exists()) {
                System.err.printf("Err: Path not found: '%s'\n", file.getAbsolutePath());
                continue;
            }
            move(file, dst);
        }
    }

    private void move(final File src, final File dst) throws IOException {
        if(src.isDirectory()) {
            if(!dst.mkdir())
                throw new IOException("mkdir failed: " + dst);            
            for(File file: src.listFiles())
                move(file, new File(dst, file.getName()));
            if(!src.delete())
                throw new IOException("delete failed: src=" + src);
        } else {		
            if(!src.renameTo(dst))  /* won't work across heterogeneous file systems */
                throw new IOException("renameTo failed: src=" + src + ", dst=" + dst);
        }
        verbosePrint("  recycled: %s", src);
    }

    private void verbosePrint(final String formatString, final Object... args) {
        if(verboseEnabled)
            System.out.printf(formatString + "\n", args);
    }
}