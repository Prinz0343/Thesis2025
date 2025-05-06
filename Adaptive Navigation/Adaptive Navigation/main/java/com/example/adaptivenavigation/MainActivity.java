package com.example.adaptivenavigation;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.inputmethod.EditorInfo;
import android.animation.ObjectAnimator;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {

    private TextView text1, text2;
    private EditText searchInput;
    private float currentFontSize = 16f;
    private int pageIndex = 0;

    private View header, bottomNav;
    private ScrollView scrollView;
    private int lastScrollY = 0;
    private boolean isNavVisible = true;
    private boolean isProgrammaticScroll = false; // 🚨 NEW: Flag to block scroll logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        header = findViewById(R.id.header_layout);
        bottomNav = findViewById(R.id.bottom_navigation);
        scrollView = findViewById(R.id.scrollView);

        // ✅ Scroll listener that skips scrolls during font size change
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (isProgrammaticScroll) return; // 🛑 Ignore programmatic scrolls

            int currentScrollY = scrollView.getScrollY();
            if (currentScrollY > lastScrollY + 10 && isNavVisible) {
                hideNavAndHeader();
            } else if (currentScrollY < lastScrollY - 10 && !isNavVisible) {
                showNavAndHeader();
            }
            lastScrollY = currentScrollY;
        });

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        searchInput = findViewById(R.id.search_input);

        ImageButton plusButton = findViewById(R.id.plus_button);
        ImageButton minusButton = findViewById(R.id.minus_button);
        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton nextButton = findViewById(R.id.next_button);
        ImageButton backButton = findViewById(R.id.back_button);

        searchInput.setVisibility(View.GONE);

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String query = searchInput.getText().toString().toLowerCase();
                searchAndHighlight(query);
                return true;
            }
            return false;
        });

        plusButton.setOnClickListener(v -> {
            if (currentFontSize < 30f) {
                currentFontSize += 2f;
                blockScrollTemporarily(); // ✅ Block false scroll detection
                updateFontSizes();
            }
        });

        minusButton.setOnClickListener(v -> {
            if (currentFontSize > 10f) {
                currentFontSize -= 2f;
                blockScrollTemporarily(); // ✅ Block false scroll detection
                updateFontSizes();
            }
        });

        searchButton.setOnClickListener(v -> {
            if (searchInput.getVisibility() == View.GONE) {
                searchInput.setVisibility(View.VISIBLE);
                searchInput.requestFocus();
            } else {
                String query = searchInput.getText().toString().trim().toLowerCase();
                searchAndHighlight(query);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (pageIndex < 2) {
                blockScrollTemporarily(); // ✅ ADD THIS
                pageIndex++;
                updateContent();
            }
        });

        backButton.setOnClickListener(v -> {
            if (pageIndex > 0) {
                blockScrollTemporarily(); // ✅ ADD THIS
                pageIndex--;
                updateContent();
            }
        });

        updateContent();
    }

    private void blockScrollTemporarily() {
        isProgrammaticScroll = true;
        scrollView.postDelayed(() -> isProgrammaticScroll = false, 300); // ⏱ Adjust if needed
    }

    private void updateFontSizes() {
        text1.setTextSize(currentFontSize);
        text2.setTextSize(currentFontSize);
    }

    private void searchAndHighlight(String query) {
        if (query.isEmpty()) {
            searchInput.setVisibility(View.GONE);
            resetHighlight();
            Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show();
            return;
        }

        text1.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        text2.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        SpannableString spannableText1 = new SpannableString(text1.getText().toString());
        SpannableString spannableText2 = new SpannableString(text2.getText().toString());

        boolean found = false;

        String text1Content = text1.getText().toString().toLowerCase();
        if (text1Content.contains(query)) {
            int start = text1Content.indexOf(query);
            int end = start + query.length();
            spannableText1.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_blue_light)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            found = true;
        }

        String text2Content = text2.getText().toString().toLowerCase();
        if (text2Content.contains(query)) {
            int start = text2Content.indexOf(query);
            int end = start + query.length();
            spannableText2.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_blue_light)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            found = true;
        }

        text1.setText(spannableText1);
        text2.setText(spannableText2);

        if (!found) {
            Toast.makeText(this, "No results found for \"" + query + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetHighlight() {
        text1.setText(text1.getText().toString());
        text2.setText(text2.getText().toString());
    }

    private void updateContent() {
        ImageView imageView = findViewById(R.id.imageView);
        ImageButton nextButton = findViewById(R.id.next_button);
        ImageButton backButton = findViewById(R.id.back_button);

        switch (pageIndex) {
            case 0:
                imageView.setImageResource(R.drawable.comscie);
                text1.setText(getString(R.string.ComputerScience));
                text2.setText(getString(R.string.brief_history_computer_science));
                break;
            case 1:
                imageView.setImageResource(R.drawable.infotech);
                text1.setText(getString(R.string.what_is_it));
                text2.setText(getString(R.string.history_of_it));
                break;
            case 2:
                imageView.setImageResource(R.drawable.csvsit);
                text1.setText(getString(R.string.cs_diff_it));
                text2.setText(getString(R.string.cs_vs_it));
                break;
            default:
                pageIndex = 0;
                updateContent();
                return;
        }

        nextButton.setAlpha(pageIndex == 2 ? 0.5f : 1.0f);
        nextButton.setEnabled(pageIndex != 2);

        backButton.setAlpha(pageIndex == 0 ? 0.5f : 1.0f);
        backButton.setEnabled(pageIndex != 0);
    }

    private void hideNavAndHeader() {
        isNavVisible = false;
        animateView(header, -header.getHeight());
        bottomNav.post(() -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
            int fullHeight = bottomNav.getHeight() + params.bottomMargin;
            animateView(bottomNav, fullHeight);
        });
    }

    private void showNavAndHeader() {
        isNavVisible = true;
        animateView(header, 0);
        animateView(bottomNav, 0);
    }

    private void animateView(View view, float translationY) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", translationY);
        animator.setDuration(300);
        animator.start();
    }
}
