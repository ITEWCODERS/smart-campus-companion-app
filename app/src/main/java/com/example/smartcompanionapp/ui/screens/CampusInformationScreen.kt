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
import com.example.smartcompanionapp.ui.theme.*

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
        Department("College of Computing and Engineering", "CCE", "Developing globally competitive engineers and IT professionals.", "cce@pnc.edu.ph", "(049) 508-0111 loc 101", R.drawable.cce),
        Department("College of Business, Accountancy, and Administration", "CBAA", "Producing business leaders and competent accountants.", "cbaa@pnc.edu.ph", "(049) 508-0111 loc 102", R.drawable.cbaa),
        Department("College of Arts, and Sciences", "CAS", "Excellence in teacher education and liberal arts foundation.", "cas@pnc.edu.ph", "(049) 508-0111 loc 103", R.drawable.cas),
        Department("College of Health and Allied Sciences", "CHAS", "Nurturing healthcare professionals for community wellness.", "chas@pnc.edu.ph", "(049) 508-0111 loc 104", R.drawable.chas),
        Department("College of Computing Studies", "CCS", "Advancing knowledge in computer science and technology.", "CCS@pnc.edu.ph", "(049) 508-0111 loc 105", R.drawable.ccs),
        Department("College of Education", "COE", "Producing the country's finest educators.", "COE@pnc.edu.ph", "(049) 508-0111 loc 105", R.drawable.coe)
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
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppSurface,
                    titleContentColor = TextPrimary
                )
            )
        } ,
        bottomBar = { CampusBottomNav(navController) },
        containerColor = AppBackground
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
        colors = CardDefaults.cardColors(containerColor = AppSurface),
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
                Text(
                    text = department.acronym,
                    style = MaterialTheme.typography.headlineSmall,
                    color = UniPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = department.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                Text(
                    text = department.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.2f))
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
            tint = AuroraSoftTeal,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
