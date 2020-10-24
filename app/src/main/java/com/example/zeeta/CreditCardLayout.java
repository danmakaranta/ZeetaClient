package com.example.zeeta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;

import androidx.appcompat.app.AppCompatActivity;

public class CreditCardLayout extends AppCompatActivity {

    //payment variables
    CardForm cardForm;
    Button buyBtn;
    AlertDialog.Builder paymentAlertBuilder;
    private AlertDialog paymentAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_layout);
        cardForm = findViewById(R.id.card_form2);
        buyBtn = findViewById(R.id.payNow);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("This number is required for SMS")
                .setup(CreditCardLayout.this);
        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardForm.isValid()) {
                    paymentAlertBuilder = new AlertDialog.Builder(CreditCardLayout.this);
                    paymentAlertBuilder.setTitle("Confirm before purchase");
                    paymentAlertBuilder.setMessage("Purchase of Connects from your card: " + cardForm.getCardNumber() + " ?");

                    paymentAlertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(CreditCardLayout.this, "Thank you for your purchase", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                    paymentAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = paymentAlertBuilder.create();
                    alertDialog.show();
                } else {
                    Toast.makeText(CreditCardLayout.this, "Please complete the form", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}