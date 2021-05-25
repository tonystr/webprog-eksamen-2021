/* Legg inn følgene HTML kode i <head></head> av index.html dokumentet:
	<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
	<script src="./validering.js"></script>
*/

const patterns = {
	'fornavn': /^[A-ZÆØÅ]['a-zæøå]*$/,
	'etternavn': /^[A-ZÆØÅ]['a-zæøå]*$/,
	'adresse': /^[\w\s\.,;:'-\d]+$/,
	'postnr': /^\d{4}$/,
	'telefonnr': /^\+?\(?\d{3}\)?[-\s\.]?\d{3}[-\s\.]?\d{2,6}$/,
	'epost': /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/,
	'volum': /^\d+([\.,]\d+)?$/,
	'vekt': /^\d+([\.,]\d+)?$/
};

function handleChange(e) {
	const id = e.target.id;
	const match = patterns[id].test(e.target.value);

	if (!match) {
		document.getElementById('registrer').disabled = true;
	}

	document.getElementById(id + '-error').innerText = match ?
		'' :
		`Feilaktig data. Sjekk at det stemmer med mønsteret: ${patterns[id]}`;
}

function validateInput(input) {
	return patterns[input.id].test(input.value);
}

function validateAll(e) {
	const inputs = document.querySelectorAll('input[type="text"]');
	const validInputs = Array.from(inputs).filter(validateInput);
	
	document.getElementById('registrer').disabled = validInputs.length != inputs.length;
}

function initValidering() {
	const inputs = Array.from(document.querySelectorAll('input[type="text"]'));

	document.getElementById('registrer').disabled = true;
	
	for (const input of inputs) {
		const errorTd = document.createElement('td');
		errorTd.style.color = 'red';
		errorTd.id = input.id + '-error';
		input.parentElement.parentElement.appendChild(errorTd);

		// Viser feilmeldinger
		input.addEventListener('change', handleChange);
		input.addEventListener('input', handleChange);

		// Disabler registrer knappen
		input.addEventListener('change', validateAll);
		input.addEventListener('input', validateAll);
		input.addEventListener('blur', validateAll);
	}
}

function postnrServerValidate(postnr, callBack) {
   $.get(`/validerpostnr?postnr=${encodeURIComponent(postnr)}`, res => {
		console.log(res);
		document.getElementById('postnr-error').innerText = res ?
			'' :
			'Postnummeret eksisterer ikke';
		if (callBack) callBack(null, res);
	}).fail(err => {
		console.error(err);
		if (callBack) callBack(err);
	});
}

function initPostnummerValidering() {
	document.getElementById('postnr').addEventListener('change', e => postnrServerValidate(e.target.value));
}

document.addEventListener('DOMContentLoaded', initPostnummerValidering);
document.addEventListener('DOMContentLoaded', initValidering);