package com.example.unisync.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Simple Data Model (No Architecture needed)
data class Department(
    val name: String,
    val acronym: String,
    val description: String,
    val email: String,
    val phone: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CampusInfoScreen(onBackClick = {})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusInfoScreen( onBackClick: () -> Unit) {
    // 2. Static Data defined directly inside the UI layer
    val departments = listOf(
        Department(
            name = "College of Computing and Engineering",
            acronym = "CCE",
            description = "Developing globally competitive engineers and IT professionals.",
            email = "cce@pnc.edu.ph",
            phone = "(049) 508-0111 loc 101"
        ),
        Department(
            name = "College of Business, Accountancy, and Administration",
            acronym = "CBAA",
            description = "Producing business leaders and competent accountants.",
            email = "cbaa@pnc.edu.ph",
            phone = "(049) 508-0111 loc 102"
        ),
        Department(
            name = "College of Education, Arts, and Sciences",
            acronym = "CEAS",
            description = "Excellence in teacher education and liberal arts foundation.",
            email = "ceas@pnc.edu.ph",
            phone = "(049) 508-0111 loc 103"
        ),
        Department(
            name = "College of Health and Allied Sciences",
            acronym = "CHAS",
            description = "Nurturing healthcare professionals for community wellness.",
            email = "chas@pnc.edu.ph",
            phone = "(049) 508-0111 loc 104"
        ),
        Department(
            name = "Institute of Computer Studies",
            acronym = "ICS",
            description = "Advancing knowledge in computer science and technology.",
            email = "ics@pnc.edu.ph",
            phone = "(049) 508-0111 loc 105"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Information") },
                // 👇 ADD THIS BLOCK STARTING HERE
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                // 👆 END OF NEW BLOCK
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006400),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF0F0F0) // Light gray background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(departments) { dept ->
                DepartmentCardUI(dept)
            }
        }
    }
}

@Composable
fun DepartmentCardUI(department: Department) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- IMAGE PLACEHOLDER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFE0E0E0)), // Placeholder Gray
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Placeholder",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Department Image",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // --- CONTENT ---
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = department.acronym,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF006400), // PnC Green
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = department.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                Text(
                    text = department.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))

                // Contact Section
                ContactLine(icon = Icons.Default.Email, text = department.email)
                Spacer(modifier = Modifier.height(8.dp))
                ContactLine(icon = Icons.Default.Call, text = department.phone)
            }
        }
    }
}

@Composable
fun ContactLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFDAA520), // PnC Gold
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCampusScreen() {
    CampusInfoScreen(onBackClick = {})
}