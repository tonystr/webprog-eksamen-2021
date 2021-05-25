package com.exam1;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class Controller {
	@Autowired
	private JdbcTemplate db;
	
	private Logger logger = LoggerFactory.getLogger(Controller.class);
	
	@GetMapping("/validerpostnr")
	public Boolean validerpostnr(String postnr, HttpServletResponse response) throws IOException {
		// Alle postnummre er nøyaktig 4 sifre i Norge
		Pattern postnrPattern = Pattern.compile("^\\d{4}$");
		Matcher postnrMatcher = postnrPattern.matcher(postnr);
		if (!postnrMatcher.matches()) {
			return false;
		}

		// Sjekk at postnummer eksisterer i databasen
		try {
			db.queryForObject("SELECT Poststed FROM Poststed WHERE Postnr=?", String.class, new Object[] { postnr });

			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/registrer")
	public void registrer(Pakke pakke, HttpServletResponse response) throws IOException {

		// Hele strengen må bestå av et sammenhengene ord, kun "word characters" er tillat ([A-Za-zØÆÅøæå]). Kun første bokstav kan være stor
		Pattern navnPattern = Pattern.compile("^[A-ZÆØÅ][a-zæøå]*$");
		// Alle postnummre er nøyaktig 4 sifre i Norge
		Pattern postnrPattern = Pattern.compile("^\\d{4}$");

		try {
			// Valider data med regex

			Matcher fornavnMatcher = navnPattern.matcher(pakke.getFornavn());
			if (!fornavnMatcher.matches()) {
				throw new IOException("Fornavnet stemmer ikke overens med navnemønsteret /^[A-ZÆØÅ][a-zæøå]*$/");
			}

			Matcher etternavnMatcher = navnPattern.matcher(pakke.getEtternavn());
			if (!etternavnMatcher.matches()) {
				throw new IOException("Etternavnet stemmer ikke overens med navnemønsteret /^[A-ZÆØÅ][a-zæøå]*$/");
			}

			Matcher postnrMatcher = postnrPattern.matcher(pakke.getPostnr());
			if (!postnrMatcher.matches()) {
				throw new IOException("Postnummeret stemmer ikke overens med postnummermønsteret /^\\d{4}$/");
			}
			
			
			// Sjekk at poststed med postnr finnes
			try {
				db.queryForObject("SELECT Poststed FROM Poststed WHERE Postnr=?", String.class, new Object[] { pakke.getPostnr() });
			} catch (Exception e) {
				throw new IOException("Postnummer eksisterer ikke i databasen");
			}
			
			// Sjekk at kunde ikke allerede er registrert
			int count = db.queryForObject(
				"SELECT COUNT(*) FROM Kunde WHERE " +
					"Fornavn=? AND " +
					"Etternavn=? AND " +
					"Adresse=? AND " +
					"Postnr=? AND " +
					"Telefonnr=? AND " +
					"Epost=?",
				Integer.class,
				pakke.getFornavn(),
				pakke.getEtternavn(),
				pakke.getAdresse(),
				pakke.getPostnr(),
				pakke.getTelefonnr(),
				pakke.getEpost()
			);
			
			if (count > 0) {
				throw new IOException("Kunde eksisterer allerede i databasen");
			}
			
			// Lagre i Kunde tabell
			db.update(
				"INSERT INTO Kunde (Fornavn, Etternavn, Adresse, Postnr, Telefonnr, Epost) VALUES (?,?,?,?,?,?)",
				pakke.getFornavn(),
				pakke.getEtternavn(),
				pakke.getAdresse(),
				pakke.getPostnr(),
				pakke.getTelefonnr(),
				pakke.getEpost()
			);
			
			// Hent Kid for nylig lagret Kunde
			int Kid = db.queryForObject(
				"SELECT Kid FROM Kunde WHERE " +
					"Fornavn=? AND " +
					"Etternavn=? AND " +
					"Adresse=? AND " +
					"Postnr=? AND " +
					"Telefonnr=? AND " +
					"Epost=?",
				Integer.class,
				pakke.getFornavn(),
				pakke.getEtternavn(),
				pakke.getAdresse(),
				pakke.getPostnr(),
				pakke.getTelefonnr(),
				pakke.getEpost()
			);
			
			// Lagre pakke
			try {
				db.update(
					"INSERT INTO Pakke (Kid, Volum, Vekt) VALUES (?,?,?)",
					Kid,
					pakke.getVolum(),
					pakke.getVekt()
				);
				System.out.println("Successfully stored data");
			} catch(Exception e) {
				// Fjern Kunde fra tabellen igjen siden en feil skjedde med insetting av Pakke
				db.update("DELETE FROM Kunde WHERE Kid=?", Kid);
				throw e;
			}
			
		} catch (Exception e) {
			String error = "Error regiserting user: " + e;
			logger.error(error);
			response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), error);
		}
	}
}

class Pakke {
	private String fornavn;
	private String etternavn;
	private String adresse;
	private String postnr;
	private String telefonnr;
	private String epost;
	private String volum;
	private String vekt;
	
	public Pakke(String fornavn, String etternavn, String adresse, String postnr, String telefonnr, String epost, String volum, String vekt) {
		this.fornavn = fornavn;
		this.etternavn = etternavn;
		this.adresse = adresse;
		this.postnr = postnr;
		this.telefonnr = telefonnr;
		this.epost = epost;
		this.volum = volum;
		this.vekt = vekt;
	}
	
	public Pakke() {}

	public String getFornavn() {
		return fornavn;
	}

	public void setFornavn(String fornavn) {
		this.fornavn = fornavn;
	}

	public String getEtternavn() {
		return etternavn;
	}

	public void setEtternavn(String etternavn) {
		this.etternavn = etternavn;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getPostnr() {
		return postnr;
	}

	public void setPostnr(String postnr) {
		this.postnr = postnr;
	}

	public String getTelefonnr() {
		return telefonnr;
	}

	public void setTelefonnr(String telefonnr) {
		this.telefonnr = telefonnr;
	}

	public String getEpost() {
		return epost;
	}

	public void setEpost(String epost) {
		this.epost = epost;
	}

	public String getVolum() {
		return volum;
	}

	public void setVolum(String volum) {
		this.volum = volum;
	}

	public String getVekt() {
		return vekt;
	}

	public void setVekt(String vekt) {
		this.vekt = vekt;
	}
	
}