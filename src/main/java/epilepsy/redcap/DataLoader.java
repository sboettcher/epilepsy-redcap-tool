package epilepsy.redcap;

import com.opencsv.CSVReaderHeaderAware;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;

public class DataLoader {
  private static final Logger LOGGER = Logger.getLogger( DictionaryLoader.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  private ArrayList<HashMap<String, String>> mData;

  private DataLoader() {
    mData = new ArrayList<>();
  }

  public DataLoader(BufferedReader reader) throws IOException {
    this();
    mData = readData(reader);
  }
  public DataLoader(File file) throws IOException {
    this();
    mData = readFromFile(file);
  }
  public DataLoader(String res) throws IOException {
    this();
    mData = readFromResource(res);
  }



  public static ArrayList<HashMap<String, String>> readData(BufferedReader reader) throws IOException {
    // check for BOM marker; will only appear as the first char sequence
    reader.mark(4);
    if ('\ufeff' != reader.read()) reader.reset(); // not the BOM marker, reset

    ArrayList<HashMap<String, String>> entries = new ArrayList<>();
    CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader);
    HashMap<String, String> next;
    while ((next = (HashMap<String, String>) csvReader.readMap()) != null) {
      entries.add(next);
    }
    LOGGER.fine(String.format("Read %d data entries", entries.size()));
    return entries;
  }

  public static ArrayList<HashMap<String, String>> readFromFile(File file) throws IOException {
    LOGGER.fine("Reading data from file " + file.getPath());
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    return readData(br);
  }
  public static ArrayList<HashMap<String, String>> readFromResource(String res) throws IOException, NullPointerException {
    LOGGER.fine("Reading data from resource " + res);
    BufferedReader br = new BufferedReader(new InputStreamReader(DictionaryLoader.class.getResourceAsStream(res), StandardCharsets.UTF_8));
    return readData(br);
  }


  public ArrayList<HashMap<String, String>> getData() {
    return mData;
  }
}
