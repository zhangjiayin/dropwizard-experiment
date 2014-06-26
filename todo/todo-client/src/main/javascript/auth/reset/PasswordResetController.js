define(function (require) {
    "use strict";
    var $ = require("jquery");
    var _ = require("underscore");
    var TboneController = require("common/TboneController");
	var RequestPasswordResetForm = require("todo/auth/reset/RequestPasswordResetForm");
	var DoPasswordResetForm = require("todo/auth/reset/DoPasswordResetForm");
	var EmailVerification = require("todo/auth/reset/EmailVerification");
	var Logger = require("common/util/Logger");

	var log = new Logger("PasswordResetController");

    return TboneController.extend({
		requestPasswordReset: function (username) {
			return $.ajax({
				url: "api/verifications/passwordreset",
				type: "POST",
				data: {
					username: username
				}
			});
		},

        changePassword: function (newPassword) {
            return this.model.changePassword(newPassword)
                .done(function () {
                    //DashboardController.show();
                });
        }
    }, {
        showRequestResetPasswordForm: function (region) {
			this._showView(region, null, RequestPasswordResetForm);
        },

        showDoPasswordResetForm: function (region, id) {
			this._showView(region, EmailVerification.fetchById(id)
				.then(function (verification) {
					if (!verification.isPasswordReset()) {
						log.error("Got email verification that was not for password reset:", verification);
						return new $.Deferred().reject();
					}
				}), DoPasswordResetForm);
        }
    });
});