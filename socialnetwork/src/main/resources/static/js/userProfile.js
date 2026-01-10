function byId(id){ return document.getElementById(id); }

var editModal = byId('editEventModal');
var closeEventEditModal = byId('closeEventEditModal');
var addEventBtn = byId('add-event-btn');

function openModal(){ if(editModal) editModal.style.display = 'flex'; }
function closeModal(){ if(editModal) editModal.style.display = 'none'; currentEditId = null; }


 if(closeEventEditModal) closeEventEditModal.addEventListener('click', function(){ closeModal(); });
 if(addEventBtn) addEventBtn.addEventListener('click', function(){ openModal(); });

 var editEvent = byId('edit-event');
 
 if(editEvent) editEvent.addEventListener('click', function(e){ callEventPage(e); })
  
 function callEventPage(el){
	let eventId = el.currentTarget.getAttribute('data-id');
	window.location.href = "/event/" + eventId;
}