package gov.epa.ghs_data_gathering.Parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import gov.epa.ghs_data_gathering.Utilities.DoubleTypeAdapter;

public class NITERecordWehage {
	
	public String ID;
	public ArrayList<String> cas_number;
	public String country;
	public String date_classified;
	public String date_imported;
	public String descriptive_name;
	public String file_path;

	public hazards hazards=new hazards();

	public int list_rating;
	public String list_type;
	public String source;
	
	public translated_data translated_data=new translated_data();
	
	public class hazards {
		
		public hazard acute_toxicity_oral;//*
		public hazard acute_toxicity_inhalation_dust;//*
		public hazard acute_toxicity_inhalation_gas;//*
		public hazard acute_toxicity_inhalation_vapor;//*
		public hazard acute_toxicity_dermal;//*
		
		public hazard aspiration_hazard;

		public hazard reproductive_toxicity;//*
		public hazard carcinogenicity;//*
		public hazard germ_cell_mutagenicity;//*

		public hazard systemic_toxicity_repeat_exposure;
		public hazard systemic_toxicity_single_exposure;

		public hazard skin_corrosion_irritation;//*
		public hazard skin_sensitizer;//*
		public hazard serious_eye_damage_irritation;//*
		public hazard respiratory_sensitizer;
		
		public hazard acute_aquatic_toxicity;//*
		public hazard chronic_aquatic_toxicity;

		public hazard corrosive_to_metals;
		public hazard explosives;
		public hazard flammable_aerosols;
		public hazard flammable_gases;
		public hazard flammable_liquids;
		public hazard flammable_solids;
		public hazard gases_under_pressure;
		public hazard hazardous_to_ozone;
		public hazard oxidizing_gases;
		public hazard oxidizing_liquids;
		public hazard oxidizing_solids;
		public hazard pyrophoric_liquids;
		public hazard pyrophoric_solids;
		public hazard self_heating_substances;
		public hazard self_reactive_substances;
		public hazard substances_mixtures_emit_flammable_gas_in_contact_with_water;
		public hazard organic_peroxides;
		
		public class hazard {
			public String classification;
			public double hazard_id; 
			public String hazard_name;
			public String hazard_statement;
			public String rationale;
			public String signal_word;
			public String symbol;
		}
	}

	
	/* From wehage:	
	 * 
	 * self.translation_patterns = {
	            '1': {
	                'Category 1A': 4,
	                'Category 1B': 4,
	                'Category 2': 3,
	                'Not classified': 2},
	            '2': {
	                'Category 1': 5,
	                'Category 2': 5,
	                'Category 3': 4,
	                'Category 4': 3,
	                'Category 5': 2,
	                'Not classified': 2},
	            '3': {
	                'Category 1': 5,
	                'Category 2': 4,
	                'Category 3': 3,
	                'Not classified': 2},
	            '4': {
	                'Category 1': 4,
	                'Category 2': 3,
	                'Not classified': 2},
	            '5': {
	                'Category 1A': 4,
	                'Category 1B': 3,
	                'Not classified': 2,
	                'Category 1': 4},
	            '6': {
	                'Category 1': 5,
	                'Category 2A': 4,
	                'Category 2B': 3,
	                'Not classified': 2},
	            '7': {
	                'Category 4': 3},
	            '8': {
	                'Not classified': 2},
	            '9': {
	                'Category 1': 3,
	                'Not classified': 2},
	            '10': {
	                'Category 1': 5,
	                'Category 2': 4,
	                'Category 3': 3,
	                'Category 4': 3,
	                'Not classified': 2},
	            '11': {
	                'Category 1': 4,
	                'Not classified': 2},
	            '12': {
	                'Category 1': 4,
	                'Category 2': 3,
	                'Category A': 3,
	                'Category B': 2,
	                'Not classified': 2},
	            '13': {
	                'Category 1': 4,
	                'Category 2': 3,
	                'Category 3': 2,
	                'Not classified': 2}}
	            
	          self.translation_criteria = {
	            'AA': {'name': 'Acute Aquatic Toxicity',
	                   'native_classification': [
	                       'acute_aquatic_toxicity'],
	                   'pattern': [self.translation_patterns['3']],
	                   'requires_parsing': False},

	            'AT': {'name': 'Acute Mammalian Toxicity',
	                   'native_classification': [
	                       'acute_toxicity_oral',
	                       'acute_toxicity_dermal',
	                       'acute_toxicity_inhalation_gas',
	                       'acute_toxicity_inhalation_vapor',
	                       'acute_toxicity_inhalation_dust'],
	                   'pattern': [self.translation_patterns['2']] * 5,
	                   'requires_parsing': False},

	            'C': {'name': 'Carcinogenicity',
	                  'native_classification': [
	                      'carcinogenicity'],
	                  'pattern': [self.translation_patterns['1']],
	                  'requires_parsing': False},

	            'CA': {'name': 'Chronic Aquatic Toxicity',
	                   'native_classification': [
	                       'chronic_aquatic_toxicity'],
	                   'pattern': [self.translation_patterns['7']],
	                   'requires_parsing': False},

	            'F': {'name': 'Flammability',
	                  'native_classification': [
	                      'flammable_gases',
	                      'flammable_aerosols',
	                      'flammable_liquids',
	                      'flammable_solids'],
	                  'pattern': [
	                      self.translation_patterns['12'],
	                      self.translation_patterns['13'],
	                      self.translation_patterns['10'],
	                      self.translation_patterns['4']],
	                  'requires_parsing': False},

	            'M': {'name': 'Mutagenicity',
	                  'native_classification': [
	                      'germ_cell_mutagenicity'],
	                  'pattern': [self.translation_patterns['1']],
	                  'requires_parsing': False},

	            'D': {'name': 'Developmental Hazard',
	                  'native_classification': [
	                      'reproductive_toxicity'],
	                  'pattern': [self.translation_patterns['1']],
	                  'requires_parsing': False},

	            'R': {'name': 'Reproductive',
	                  'native_classification': [
	                      'reproductive_toxicity'],
	                  'pattern': [self.translation_patterns['1']],
	                  'requires_parsing': False},

	            'Rx': {'name': 'Reactivity',
	                   'native_classification': [
	                       'explosives',
	                       'self_reactive_substances',
	                       'substances_mixtures_emit_flammable' +
	                       '_gas_in_contact_with_water',
	                       'oxidizing_gases',
	                       'oxidizing_solids',
	                       'organic_peroxides',
	                       'self_heating_substances',
	                       'corrosive_to_metals'],
	                   'pattern': [
	                       self.translation_patterns['8'],
	                       self.translation_patterns['8'],
	                       self.translation_patterns['3'],
	                       self.translation_patterns['11'],
	                       self.translation_patterns['3'],
	                       self.translation_patterns['3'],
	                       self.translation_patterns['8'],
	                       self.translation_patterns['4'],
	                       self.translation_patterns['9']],
	                   'requires_parsing': False},

	            'SnR': {'name': 'Sensitization, Respiratory',
	                    'native_classification': [
	                        'respiratory_sensitizer'],
	                    'pattern': [self.translation_patterns['5']],
	                    'requires_parsing': False},

	            'SnS': {'name': 'Sensitization, Skin',
	                    'native_classification': [
	                        'skin_sensitizer'],
	                    'pattern': [self.translation_patterns['5']],
	                    'requires_parsing': False},

	            'E': {'name': 'Endocrine Activity',
	                  'native_classification': [],
	                  'pattern': [],
	                  'requires_parsing': False},

	            'P': {'name': 'Persistence',
	                  'native_classification': [],
	                  'pattern': [],
	                  'requires_parsing': False},

	            'B': {'name': 'Bioaccumulation',
	                  'native_classification': [],
	                  'pattern': [],
	                  'requires_parsing': False},

	            'IrS': {'name': 'Skin Irritation',
	                    'native_classification': [
	                        'skin_corrosion_irritation'],
	                    'pattern': [self.translation_patterns['3']],
	                    'requires_parsing': False},

	            'IrE': {'name': 'Eye Irritation',
	                    'native_classification': [
	                        'serious_eye_damage_irritation'],
	                    'pattern': [self.translation_patterns['6']],
	                    'requires_parsing': False},

	            # In the following four cases, multiple greenscreen hazards
	            # are stored under a single ghs japan hazard. A regular expression
	            # is provided that uniquely identifies each hazard group.
	            # The hazard is only assigned to the greenscreen hazard if
	            # one of the provided keywords is found in the grouping

	            # Neurotoxicity / systemic toxicity, repeat exposure
	            # are grouped together under 'systemic_toxicity_repeat_exposure'
	            'N_r': {'name': 'Neurotoxicity, Repeat Exposure',
	                    'native_classification': [
	                        'systemic_toxicity_repeat_exposure'],
	                    'pattern': [self.translation_patterns['4']],
	                    'requires_parsing': True,
	                    'regex': regex,
	                    'keywords': self.neuro_keywords},

	            'ST_r': {'name': 'Systemic Toxicity, Repeat Exposure',
	                     'native_classification': [
	                         'systemic_toxicity_repeat_exposure'],
	                     'pattern': [self.translation_patterns['3']],
	                     'requires_parsing': True,
	                     'regex': regex,
	                     'keywords': self.systemic_keywords},

	            # Neurotoxicity / systemic toxicity, single exposure
	            # are grouped together under 'systemic_toxicity_single_exposure'
	            'N_s': {'name': 'Neurotoxicity, Single Exposure',
	                    'native_classification': [
	                        'systemic_toxicity_single_exposure'],
	                    'pattern': [self.translation_patterns['4']],
	                    'requires_parsing': True,
	                    'regex': regex,
	                    'keywords': self.neuro_keywords},

	            'ST_s': {'name': 'Systemic Toxicity, Single Exposure',
	                     'native_classification': [
	                         'systemic_toxicity_single_exposure'],
	                     'pattern': [self.translation_patterns['3']],
	                     'requires_parsing': True,
	                     'regex': regex,
	                     'keywords': self.systemic_keywords}}
	 * 
	 * 
	 */ 		
	
	public class translated_data {
		

        public int AT;//acute mammalian toxicity
        
        public int C;//carcinogenicity
        public int D;//developmental hazard
        public int R;//reproductive
        public int M;//mutagenicity
        public int E;//endocrine activity

        public int SnR;//respiratory sensitization
        public int SnS;//skin sensitization
        public int IrE;//eye irritation
        public int IrS;//skin irritation

        public int N_r;//Neurotoxicity, Repeat Exposure
        public int N_s;//Neurotoxicity, Single Exposure
        public int ST_r;//Systemic Toxicity, Repeat Exposure
        public int ST_s;//Systemic Toxicity, Single Exposure

        public int AA;//acute aquatic toxicity
        public int CA;//chronic aquatic toxicity

        public int P;//persistence
        public int B;//bioaccumulation

        public int F;//flammability
        public int Rx;//reactivity

	}
	
	public static NITERecordWehage loadNITEJSon(File jsonFile) {
		
		try {
			
			if (!jsonFile.exists()) return null;

//			Gson gson = new Gson();
			
//			Gson gson = new GsonBuilder()
//					.registerTypeAdapter(double.class, new DoubleTypeAdapter())
//					.registerTypeAdapter(Double.class, new DoubleTypeAdapter())
//					.registerTypeAdapter(int.class, new IntegerTypeAdapter())
//				    .registerTypeAdapter(Integer.class, new IntegerTypeAdapter()).create();
			
//			Gson gson = new GsonBuilder()
//					.registerTypeAdapter(double.class, new DoubleTypeAdapter())
//					.registerTypeAdapter(int.class, new IntegerTypeAdapter()).create();
			
			//seems like we only need to register double to get it to work:
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(double.class, new DoubleTypeAdapter()).create();

			
			NITERecordWehage n=gson.fromJson(new FileReader(jsonFile), NITERecordWehage.class);

			//test it to see if it outputs back out correctly:
//			System.out.println(n.toJSON());

//			System.out.println(n.hazards.acute_aquatic_toxicity.classification);
			
			return n;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Output class as a JSON string- lets you test if parse went ok and everything got assigned right
	 * 
	 * Note: the order of the fields at the top of class determine the order they are output
	 *  
	 * @return
	 */
	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().serializeNulls();
		
		builder.setFieldNamingStrategy(new FieldNamingStrategy() { 
			@Override
			public String translateName(Field field) {
				//use this if we want to rename anything before outputting:
				if (field.getName().equals("BOB"))
					return "bob";
				else
					return field.getName();
			}
		});
		
		Gson gson = builder.create();
		return gson.toJson(this);
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
