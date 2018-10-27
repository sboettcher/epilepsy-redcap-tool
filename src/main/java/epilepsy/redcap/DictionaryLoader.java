package epilepsy.redcap;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;

public class DictionaryLoader {
  private static final Logger LOGGER = Logger.getLogger( DictionaryLoader.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  private ArrayList<DictionaryEntry> mEntries;
  private ArrayList<String> mInstrumentOrder;
  private HashMap<String, ArrayList<DictionaryEntry>> mInstruments;

  private DictionaryLoader() {
    mEntries = new ArrayList<>();
    mInstrumentOrder = new ArrayList<>();
    mInstruments = new HashMap<>();
  }

  public DictionaryLoader(Reader reader) {
    this();
    mEntries = (ArrayList<DictionaryEntry>) readDictionary(reader);
    processInstruments();
  }
  public DictionaryLoader(File file) throws FileNotFoundException {
    this();
    mEntries = (ArrayList<DictionaryEntry>) readFromFile(file);
    processInstruments();
  }
  public DictionaryLoader(String res) {
    this();
    mEntries = (ArrayList<DictionaryEntry>) readFromResource(res);
    processInstruments();
  }

  private void processInstruments() {
    // sort dictionary entries into instruments map and keep order
    for (DictionaryEntry de : mEntries) {
      if (!mInstruments.containsKey(de.getFormName())) {
        mInstruments.put(de.getFormName(), new ArrayList<>());
        mInstrumentOrder.add(de.getFormName());
      }
      mInstruments.get(de.getFormName()).add(de);
    }
  }



  public static List<DictionaryEntry> readDictionary(Reader reader) {
    List<DictionaryEntry> entries = new CsvToBeanBuilder(reader).withType(DictionaryEntry.class).build().parse();
    entries.add(1, new DictionaryEntry("redcap_repeat_instrument", "study_information", "text"));
    entries.add(2, new DictionaryEntry("redcap_repeat_instance", "study_information", "text"));
    for (DictionaryEntry entry : entries)
      entry.processChoices();
    LOGGER.fine(String.format("Read %d dictionary entries", entries.size()));
    return entries;
  }

  public static List<DictionaryEntry> readFromFile(File file) throws FileNotFoundException {
    LOGGER.fine("Reading dictionary from file " + file.getPath());
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    return readDictionary(br);
  }
  public static List<DictionaryEntry> readFromResource(String res) throws NullPointerException {
    LOGGER.fine("Reading dictionary from resource " + res);
    BufferedReader br = new BufferedReader(new InputStreamReader(DictionaryLoader.class.getResourceAsStream(res)));
    return readDictionary(br);
  }


  public ArrayList<DictionaryEntry> getEntries() {
    return mEntries;
  }

  public ArrayList<String> getInstrumentOrder() {
    return mInstrumentOrder;
  }

  public HashMap<String, ArrayList<DictionaryEntry>> getInstruments() {
    return mInstruments;
  }
}
