$(document).ready(function () {
  var trigger = $('.hamburger'),
      overlay = $('.overlay'),
	  wrapper = $('#wrapper'),
	  sidebar = $('#sidebar-wrapper'),
     isClosed = false;

    trigger.click(function () {
      hamburger_cross();      
    });
	
	trigger.on({
			mouseenter: function(){
			hamburger_cross(); 			
		}
	});

    function hamburger_cross() {

      if (isClosed == true) {          
        closeMenu();
      } else {   
		openMenu();        
      }	  
  }
  
  function closeMenu() {
	overlay.hide();
	trigger.removeClass('is-open');
	trigger.addClass('is-closed');	
	wrapper.removeClass('toggled');
	isClosed = false;
  }
  
  function openMenu() {
	overlay.show();
	trigger.removeClass('is-closed');
	trigger.addClass('is-open');	
	wrapper.addClass('toggled');
	isClosed = true;	
  }
  
  sidebar.on({
    mouseleave: function(){
      closeMenu();
    }    
});
  
  $('[data-toggle="offcanvas"]').click(function () {
        $('#wrapper').toggleClass('toggled');
  });  
});