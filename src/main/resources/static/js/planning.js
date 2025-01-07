//Contrôle sur saisi du formulaire de planning
document.addEventListener('DOMContentLoaded', function(){
    const heure = document.getElementById('planning_heure');
    const interval = document.getElementById('planning_interval');
    const always = document.getElementById('alwaysBox');

    const toggleFields = () => {
        const isDisabled = always.checked;
        heure.disabled = isDisabled;
        interval.disabled = isDisabled;
    };

    // Initial state
    toggleFields();

    // On checkbox change
    always.addEventListener('change', toggleFields);
});


//Ajouter l'envoi du formulaire au click du button pour plannifier
document.getElementById('planning_btn').addEventListener('onClick',function(event){

    //Récupérer les informations du formulaire de planning
    const heure = document.getElementById('planning_heure').value.trim();
    const interval = document.getElementById('planning_interval').value.trim();
    const always = document.getElementById('alwaysBox').value.trim();

    if(always.isChecked){
        always = true;
    }else{
        always = false;
    }


    console.log(heure);
    console.log(interval);
    console.log(always);

    //Envoie les informations
    fetch("/make-planning", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `planning_heure=${encodeURIComponent(heure)}}&planning_interval=${encodeURIComponent(interval)}&always=${encodeURIComponent(always)}`
        })
        .then(message =>{alert(message)})
        .catch(error => {
                alert("Erreur lors de l'enregistrement :", error);
            });
        });