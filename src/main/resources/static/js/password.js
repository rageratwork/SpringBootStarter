$(function(){
    var enableSubmit = function() {
        var oldPassword = $("#oldPassword").val();
        var password = $("#newPassword").val();
        var confirm = $("#confirmPassword").val();

        $("#submit").prop("disabled", password.length == 0 || password.length == 0 || password != confirm);
    }

    $("#newPassword").keyup(function() {
        var strength = 0;
        var message = "";
        var password = $("#newPassword").val();

        var len = password.trim().length
        strength += Math.min(len, 8);

        var re = /[\s~`!@#$%\^&*+=\-\[\]\\';,/{}|\\":<>\?()\._]/;
        if(re.test(password)) {
            strength++;
        }

        re = /[A-Z]/;
        if(re.test(password)) {
            strength++;
        }

        re = /[0-9]/;
        if(re.test(password)) {
            strength++;
        }

        if(strength <= 8) message = "poor";
        else if(strength == 9) message = "weak";
        else if(strength == 10) message = "strong";
        else message = "very strong";

        $("#strength").html(message);

        enableSubmit();
    });

    $("#confirmPassword").keyup(function() {
        enableSubmit();
    });

    enableSubmit();
});
