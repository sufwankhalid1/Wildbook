package org.ecocean.email.old;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
   * {@code Authenticator} instance to allow SMTP Authentication.
   */
  final class SMTPAuthenticator extends Authenticator {
    private final PasswordAuthentication pa;

    SMTPAuthenticator(final String u, final String p) {
        pa = new PasswordAuthentication(u, p);
    }


    public PasswordAuthentication getPA() {
        return pa;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      return pa;
    }
  }