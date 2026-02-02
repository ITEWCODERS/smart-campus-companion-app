package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.R
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav

data class Department(
    val name: String,
    val acronym: String,
    val description: String,
    val email: String,
    val phone: String,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusInfoScreen(navController: NavController) {
    val departments = listOf(
        Department(
            name = "College of Computing and Engineering",
            acronym = "CCE",
            description = "Developing globally competitive engineers and IT professionals.",
            email = "cce@pnc.edu.ph",
            phone = "(049) 508-0111 loc 101",
            imageRes = R.drawable.cce
        ),
        Department(
            name = "College of Business, Accountancy, and Administration",
            acronym = "CBAA",
            description = "Producing business leaders and competent accountants.",
            email = "cbaa@pnc.edu.ph",
            phone = "(049) 508-0111 loc 102",
            imageRes = R.drawable.cbaa
        ),
        Department(
            name = "College of Arts, and Sciences",
            acronym = "CAS",
            description = "Excellence in teacher education and liberal arts foundation.",
            email = "cas@pnc.edu.ph",
            phone = "(049) 508-0111 loc 103",
            imageRes = R.drawable.cas
        ),
        Department(
            name = "College of Health and Allied Sciences",
            acronym = "CHAS",
            description = "Nurturing healthcare professionals for community wellness.",
            email = "chas@pnc.edu.ph",
            phone = "(049) 508-0111 loc 104",
            imageRes = R.drawable.chas
        ),
        Department(
            name = "College of Computing Studies",
            acronym = "CCS",
            description = "Advancing knowledge in computer science and technology.",
            email = "CCS@pnc.edu.ph",
            phone = "(049) 508-0111 loc 105",
            imageRes = R.drawable.ccs
        ),
        Department(
            name = "College of Education",
            acronym = "COE",
            description = "Producing the country's finest educators.",
            email = "COE@pnc.edu.ph",
            phone = "(049) 508-0111 loc 105",
            imageRes = R.drawable.coe
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006400),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { CampusBottomNav(navController) },
        containerColor = Color(0xFFF0F0F0)
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
            Image(
                painter = painterResource(id = department.imageRes),
                contentDescription = "${department.acronym} Building",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = department.acronym,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF006400),
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
            tint = Color(0xFFDAA520),
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
