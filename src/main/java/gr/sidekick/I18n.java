package gr.sidekick;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** UI language is independent of user-entered names and assignment IDs. */
public final class I18n {
    private static volatile String language="el";
    private static final Map<String,String> EN=load();
    private I18n() {}
    private static Map<String,String> load(){
        try(InputStream in=Objects.requireNonNull(I18n.class.getResourceAsStream("/i18n/en.json"));Reader reader=new InputStreamReader(in,StandardCharsets.UTF_8)){
            return new Gson().fromJson(reader,new TypeToken<Map<String,String>>(){}.getType());
        }catch(IOException e){throw new ExceptionInInitializerError(e);}
    }
    public static String language(){return language;}
    public static Locale locale(){return Locale.forLanguageTag(language);}
    public static boolean english(){return "en".equals(language);}
    public static String text(String greek){return english()?EN.getOrDefault(greek,greek):greek;}
    public static String greek(String label){if(!english())return label;return EN.entrySet().stream().filter(e->e.getValue().equals(label)).map(Map.Entry::getKey).findFirst().orElse(label);}
    public static void setLanguage(String value){
        language="en".equals(value)?"en":"el";
        JComponent.setDefaultLocale(locale());JOptionPane.setDefaultLocale(locale());
        String[][] controls={
            {"OptionPane.okButtonText","ΟΚ","OK"},{"OptionPane.cancelButtonText","Ακύρωση","Cancel"},{"OptionPane.yesButtonText","Ναι","Yes"},{"OptionPane.noButtonText","Όχι","No"},
            {"FileChooser.saveButtonText","Αποθήκευση","Save"},{"FileChooser.openButtonText","Άνοιγμα","Open"},{"FileChooser.cancelButtonText","Ακύρωση","Cancel"},
            {"FileChooser.saveDialogTitleText","Αποθήκευση PDF","Save PDF"},{"FileChooser.lookInLabelText","Φάκελος:","Look in:"},{"FileChooser.saveInLabelText","Αποθήκευση σε:","Save in:"},
            {"FileChooser.fileNameLabelText","Όνομα αρχείου:","File name:"},{"FileChooser.filesOfTypeLabelText","Τύπος αρχείου:","Files of type:"},
            {"FileChooser.upFolderToolTipText","Προηγούμενος φάκελος","Up one level"},{"FileChooser.homeFolderToolTipText","Αρχικός φάκελος","Home"},{"FileChooser.newFolderToolTipText","Νέος φάκελος","Create new folder"},
            {"FileChooser.listViewButtonToolTipText","Λίστα","List"},{"FileChooser.detailsViewButtonToolTipText","Λεπτομέρειες","Details"}
        };
        for(String[] row:controls)UIManager.put(row[0],row[english()?2:1]);
    }
}
