package semanticMarkup.io.input.lib.taxonx;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import semanticMarkup.io.input.AbstractFileVolumeReader;
import semanticMarkup.model.Treatment;
import semanticMarkup.model.ValueTreatmentElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;
/**
 * TaxonxVolumeReader reads a list of treatments of type 4 that are valid against the XML Schema resources/io/taxonx/taxonx1.xsd
 * TODO: Which elements are relevant, need to be translated to treatmentElements and present in output?
 * @author rodenhausen
 */
public class TaxonxVolumeReader extends AbstractFileVolumeReader {

	/**
	 * @param sourceFile
	 */
	@Inject
	public TaxonxVolumeReader(@Named("TaxonxVolumeReader_SourceFile")String sourceFile) {
		super(sourceFile);
	}

	@Override
	public List<Treatment> read() throws Exception {
		JAXBContext jc = JAXBContext.newInstance("semanticMarkup.io.input.lib.taxonx");
        Unmarshaller u = jc.createUnmarshaller();
        File file = new File(filePath);
	    semanticMarkup.io.input.lib.taxonx.Taxonx taxonx = (semanticMarkup.io.input.lib.taxonx.Taxonx)u.unmarshal(file);
	    return transformToTreatments(taxonx);
	}

	private List<Treatment> transformToTreatments(Taxonx taxonx) {
	    List<Treatment> result = new ArrayList<Treatment>();
		
		for(Object divOrPOrHead : taxonx.getTaxonxBody().getDivOrPOrHead()) {			
			
			if(divOrPOrHead instanceof semanticMarkup.io.input.lib.taxonx.Treatment) {
				semanticMarkup.io.input.lib.taxonx.Treatment xmlTreatment = (semanticMarkup.io.input.lib.taxonx.Treatment)divOrPOrHead;
				Treatment treatment = new Treatment();
				extractTreatmentElements(xmlTreatment, treatment);
				result.add(treatment);
			}
			
			
			/*
			if(divOrPOrHead instanceof P) {
				P p = (P) divOrPOrHead;
				List<Serializable> content = p.getContent();
				for(Serializable contentElement : content) {
					if(contentElement instanceof JAXBElement) {
						JAXBElement jaxbElement = (JAXBElement)contentElement;
						if(jaxbElement.getDeclaredType().equals(Note.class)) {
							JAXBElement<Note> typedJaxbElement = (JAXBElement<Note>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Nomenclature.class)) {
							JAXBElement<Nomenclature> typedJaxbElement = (JAXBElement<Nomenclature>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Seg.class)) {
							JAXBElement<Seg> typedJaxbElement = (JAXBElement<Seg>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Name.class)) {
							JAXBElement<Name> typedJaxbElement = (JAXBElement<Name>)jaxbElement;
							Name name = typedJaxbElement.getValue();
							List<Serializable> nameContent = name.getContent();
							for(Serializable nameContentElement : nameContent) {
								if(nameContentElement instanceof JAXBElement) {
									JAXBElement nameJaxbElement = (JAXBElement)nameContentElement;
									System.out.println(nameJaxbElement.getDeclaredType());
									if(nameJaxbElement.getDeclaredType().equals(Xmldata.class)) {
										JAXBElement<Xmldata> typedNameJaxbElement = (JAXBElement<Xmldata>)nameJaxbElement;
										Xmldata xmldata = typedNameJaxbElement.getValue();
									}
									if(nameJaxbElement.getDeclaredType().equals(Xid.class)) {
										JAXBElement<Xid> typedNameJaxbElement = (JAXBElement<Xid>)nameJaxbElement;
										Xid xid = typedNameJaxbElement.getValue();
									}
									if(nameJaxbElement.getDeclaredType().equals(Pb.class)) {
										JAXBElement<Pb> typedNameJaxbElement = (JAXBElement<Pb>)nameJaxbElement;
										Pb pb = typedNameJaxbElement.getValue();
									}
									if(nameJaxbElement.getDeclaredType().equals(Figure.class)) {
										JAXBElement<Figure> typedNameJaxbElement = (JAXBElement<Figure>)nameJaxbElement;
										Figure figure = typedNameJaxbElement.getValue();
									}
								} else {
									//System.out.println("just a string " + nameContentElement);
								}
							}
						}
						if(jaxbElement.getDeclaredType().equals(Locality.class)) {
							JAXBElement<Locality> typedJaxbElement = (JAXBElement<Locality>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Character.class)) {
							JAXBElement<Character> typedJaxbElement = (JAXBElement<Character>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Pb.class)) {
							JAXBElement<Pb> typedJaxbElement = (JAXBElement<Pb>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(CollectionEvent.class)) {
							JAXBElement<CollectionEvent> typedJaxbElement = (JAXBElement<CollectionEvent>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Xmldata.class)) {
							JAXBElement<Xmldata> typedJaxbElement = (JAXBElement<Xmldata>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Figure.class)) {
							JAXBElement<Figure> typedJaxbElement = (JAXBElement<Figure>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Bibref.class)) {
							JAXBElement<Bibref> typedJaxbElement = (JAXBElement<Bibref>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Figure.class)) {
							JAXBElement<Figure> typedJaxbElement = (JAXBElement<Figure>)jaxbElement;
						}
					} else {
						//System.out.println("just a string " + contentElement);
					}
				}
			} */
		}
		
		//assign treatment names
		int treatmentsSize = result.size();
		int nameLength = String.valueOf(treatmentsSize).length();
		for(int i=0; i<result.size(); i++) {
			Treatment treatment = result.get(i);
			treatment.setName(intToString(i, nameLength));
		}
		
		return result;
	}
	
	private String intToString(int num, int digits) {
		// create variable length array of zeros
	    char[] zeros = new char[digits];
	    Arrays.fill(zeros, '0');
	    // format number as String
	    DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

	    return df.format(num);
	}

	private void extractTreatmentElements(semanticMarkup.io.input.lib.taxonx.Treatment xmlTreatment, Treatment treatment) {
		//System.out.println(treatment.getTaxonxHeader());
		//System.out.println(treatment.getHead());
		//System.out.println(treatment.getOtherRank());
		//System.out.println(treatment.getRank());
		//System.out.println(treatment.getNomenclatureOrDivOrRefGroup());
		
		List<Object> nomenclatureOrDivOrRefGroups = xmlTreatment.getNomenclatureOrDivOrRefGroup();
		for(Object nomenclatureOrDivOrRefGroup : nomenclatureOrDivOrRefGroups) {
			if(nomenclatureOrDivOrRefGroup instanceof Nomenclature) {
				/*Nomenclature nomenclature = (Nomenclature)nomenclatureOrDivOrRefGroup;
				List<Serializable> nomenclatureContent = nomenclature.getContent();
				for(Serializable nomenclatureContentElement : nomenclatureContent) {
					if(nomenclatureContentElement instanceof JAXBElement) {
						JAXBElement jaxbElement = (JAXBElement) nomenclatureContentElement;
						System.out.println("jaxbType " + jaxbElement.getDeclaredType());
					} else {
						//just a string
						System.out.println(nomenclatureContentElement);
					}
				}*/
			}
			if(nomenclatureOrDivOrRefGroup instanceof Div) {
				Div div = (Div)nomenclatureOrDivOrRefGroup;
				//System.out.println("div Type " + div.getType());
				
				String content = extractContent(div);
				treatment.addTreatmentElement(new ValueTreatmentElement(div.getType(), content));
			}
		}
	}

	private String extractContent(Div div) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Object> pOrDivOrPbs = div.getPOrDivOrPb();
		for(Object pOrDivOrPb : pOrDivOrPbs) {
			if(pOrDivOrPb instanceof P) {
				P p = (P) pOrDivOrPb;
				List<Serializable> pContent = p.getContent();
				for(Serializable pContentElement : pContent) {
					if(pContentElement instanceof JAXBElement) {
						JAXBElement jaxbElement = (JAXBElement)pContentElement;
						if(jaxbElement.getDeclaredType().equals(Note.class)) {
							JAXBElement<Note> typedJaxbElement = (JAXBElement<Note>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Nomenclature.class)) {
							JAXBElement<Nomenclature> typedJaxbElement = (JAXBElement<Nomenclature>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Seg.class)) {
							JAXBElement<Seg> typedJaxbElement = (JAXBElement<Seg>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Name.class)) {
							JAXBElement<Name> typedJaxbElement = (JAXBElement<Name>)jaxbElement;
							Name name = typedJaxbElement.getValue();
							List<Serializable> nameContent = name.getContent();
							for(Serializable nameContentElement : nameContent) {
								if(nameContentElement instanceof JAXBElement) {
									JAXBElement nameJaxbElement = (JAXBElement)nameContentElement;
									if(nameJaxbElement.getDeclaredType().equals(Xmldata.class)) {
										JAXBElement<Xmldata> typedNameJaxbElement = (JAXBElement<Xmldata>)nameJaxbElement;
										Xmldata xmldata = typedNameJaxbElement.getValue();
										List<Object> any = xmldata.getAny();
										for(Object anyElement : any) {
											if(anyElement instanceof String) {
												stringBuilder.append(" " + (String)anyElement);
											}
										}
									}
									if(nameJaxbElement.getDeclaredType().equals(Xid.class)) {
										JAXBElement<Xid> typedNameJaxbElement = (JAXBElement<Xid>)nameJaxbElement;
										Xid xid = typedNameJaxbElement.getValue();
										stringBuilder.append(" " + xid.getContent());
									}
									if(nameJaxbElement.getDeclaredType().equals(Pb.class)) {
										JAXBElement<Pb> typedNameJaxbElement = (JAXBElement<Pb>)nameJaxbElement;
										Pb pb = typedNameJaxbElement.getValue();
									}
									if(nameJaxbElement.getDeclaredType().equals(Figure.class)) {
										JAXBElement<Figure> typedNameJaxbElement = (JAXBElement<Figure>)nameJaxbElement;
										Figure figure = typedNameJaxbElement.getValue();
									}
								} else {
									stringBuilder.append(" " + nameContentElement.toString());
								}
							}
						}
						if(jaxbElement.getDeclaredType().equals(Locality.class)) {
							JAXBElement<Locality> typedJaxbElement = (JAXBElement<Locality>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Character.class)) {
							JAXBElement<Character> typedJaxbElement = (JAXBElement<Character>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Pb.class)) {
							JAXBElement<Pb> typedJaxbElement = (JAXBElement<Pb>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(CollectionEvent.class)) {
							JAXBElement<CollectionEvent> typedJaxbElement = (JAXBElement<CollectionEvent>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Xmldata.class)) {
							JAXBElement<Xmldata> typedJaxbElement = (JAXBElement<Xmldata>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Figure.class)) {
							JAXBElement<Figure> typedJaxbElement = (JAXBElement<Figure>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Bibref.class)) {
							JAXBElement<Bibref> typedJaxbElement = (JAXBElement<Bibref>)jaxbElement;
						}
						if(jaxbElement.getDeclaredType().equals(Figure.class)) {
							JAXBElement<Figure> typedJaxbElement = (JAXBElement<Figure>)jaxbElement;
						}
					} else {
						//just a string
						stringBuilder.append(" " + pContentElement.toString());
					}
				}
			}
			/*if(pOrDivOrPb instanceof Div) {
				Div divElement = (Div) pOrDivOrPb;
				System.out.println("divElement " + divElement);
			}
			if(pOrDivOrPb instanceof Pb) {
				Pb pb = (Pb) pOrDivOrPb;
				System.out.println("pbElement " + pb);
			}*/
		}
		
		String result = stringBuilder.toString();
		result = result.replaceAll("\n", " ");
		result = result.trim().replaceAll(" +", " ");
		return result;
	}
}