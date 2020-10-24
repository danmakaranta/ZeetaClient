package com.example.zeeta;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class AddPaymentCard extends AppCompatActivity {

    //payment variables
    CardForm cardForm;
    Card card;
    AlertDialog.Builder paymentAlertBuilder;
    private ProgressDialog transactionProgressDialog;
    private Button addCardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment_card);

        transactionProgressDialog = new ProgressDialog(this);
        transactionProgressDialog.setMessage("Please wait...");
        addCardBtn = findViewById(R.id.addCard);

        PaystackSdk.initialize(getApplicationContext());
        cardForm = findViewById(R.id.card_form3);

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("This number is required for SMS")
                .setup(AddPaymentCard.this);

        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        addCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardForm.isValid()) {
                    card = new Card(cardForm.getCardNumber(), Integer.parseInt(cardForm.getExpirationMonth()),
                            Integer.parseInt(cardForm.getExpirationYear()), cardForm.getCvv());
                    if (card.isValid()) {
                        performCharge();
                    } else {
                        Toast.makeText(AddPaymentCard.this, "Card invalid", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    cardForm.validate();
                }
            }
        });

    }

    private void performCharge() {

        //create a Charge object
        Charge charge = new Charge();
        charge.setCard(card); //sets the card to charge
        charge.setEmail("kwadagonigerialtd@gmail.com"); //dummy email address
        charge.setAmount((int) (100)); //charge amount

        DocumentReference newCard = FirebaseFirestore.getInstance()
                .collection("Payments").document(Objects.requireNonNull("Cards"))
                .collection("Customers").document(FirebaseAuth.getInstance().getUid()).collection(card.getType()).document(card.getNumber());

        PaystackSdk.chargeCard(AddPaymentCard.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {

                newCard.set(card).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        transactionProgressDialog.dismiss();
                        Toast.makeText(AddPaymentCard.this, "Card added successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }

            @Override
            public void beforeValidate(Transaction transaction) {

            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                Toast.makeText(AddPaymentCard.this, "Unsuccessful: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}