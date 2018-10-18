package epilepsy.redcap;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;

public class DictionaryLoader {
  private static final Logger LOGGER = Logger.getLogger( DictionaryLoader.class.getName() );
  static {LOGGER.setLevel(loglvl);}

  public static List<DictionaryEntry> readFromResource(String res) {
    LOGGER.fine("Reading data dictionary from resource " + res);
    BufferedReader br = new BufferedReader(new InputStreamReader(DictionaryLoader.class.getResourceAsStream(res)));
    List<DictionaryEntry> entries = new CsvToBeanBuilder(br).withType(DictionaryEntry.class).build().parse();
    LOGGER.fine(String.format("Read %d entries from %s",entries.size(), res));
    return entries;
  }
}
