$(document).ready(function(){
    $('.sidenav').sidenav();

    // var prevScrollpos = window.pageYOffset;
    window.onscroll = function() {

        var isHeaderVisible = isElementInViewport(document.getElementById('header'));
        console.log('Element visible', isHeaderVisible);
        if (isHeaderVisible) {
            $('#sidebar').css('position', 'relative');
        } else {
            $('#sidebar').css('position', 'fixed');
            $('#sidebar').css('top', '0');
        }

        // var currentScrollPos = window.pageYOffset;
        // var isGoingDown = (prevScrollpos < currentScrollPos);
        // if (isGoingDown) {
            // $('#header').css('top', '-64px');
            // $('#sidebar').css('top', '0');
            // $('#sidebar-toggle').css('top', '0');
        // } else {
            // $('#header').css('top', '0');
            // $('#sidebar').css('top', '64px');
            // $('#sidebar-toggle').css('top', '64px');
        // }
        // prevScrollpos = currentScrollPos;
    };
});

function isElementInViewport(el) {
    var rect = el.getBoundingClientRect();

    return rect.bottom > 0 && rect.right > 0 &&
           rect.left < $(window).width() && rect.top < $(window).height();
}

function toggleSidebar() {
    var sidenav = M.Sidenav.getInstance(document.getElementById('sidebar'));
    if (sidenav.isOpen) {
        // sidenav.close();
        $('#layout').addClass('no-sidebar')
    }  else {
        // sidenav.open();
        $('#layout').removeClass('no-sidebar')
    }
}