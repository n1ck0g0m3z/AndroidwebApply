// JavaScript Document

$(function() {
$( "#datepicker" ).datepicker();
});

$(function(){
	$("#states_customerBtn").on("click", function() {
		$(this).next().slideToggle();
		$(this).toggleClass("active");//’Ç‰Á•”•ª
	});
});
/*
$(window).bind('scroll', function () {
    if ($(window).scrollTop() > 0) {
        $('header').addClass('header2');
    } else {
        $('header').removeClass('header2');
    }
});*/




