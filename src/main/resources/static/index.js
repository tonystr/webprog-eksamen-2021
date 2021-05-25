/* Legg inn følgene HTML kode i <head></head> av index.html dokumentet:
	<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
	<script src="./index.js"></script>
*/


function registerInputs() {
	const inputs = Array.from(document.querySelectorAll('input[type="text"]'));
	let dataIsValid = true;

	const data = {};
	for (const input of inputs) {
		data[input.id] = input.value;
		if (!validateInput(input)) dataIsValid = false;
	}

	if (dataIsValid) {
		postnrServerValidate(document.getElementById('postnr').value, res => {
			if (res) {
				$.post('/registrer', data, console.log);
				console.log('postnr is valid, posted');
			} else {
				console.log('postnr is NOT valid');
			}
		});
	} else {
		console.log('data is NOT valid');
	}
}


function init() {
	document.getElementById('registrer').addEventListener('click', registerInputs);
}

document.addEventListener('DOMContentLoaded', init);