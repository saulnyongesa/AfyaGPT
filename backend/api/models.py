from django.db import models

class UserProfile(models.Model):
    """Health worker profile model (CHW, Nurse, Clinical Officer)."""
    full_name = models.CharField(max_length=255)
    phone_number = models.CharField(max_length=20, unique=True)
    email = models.EmailField(blank=True, null=True)
    profession = models.CharField(max_length=100)
    professional_number = models.CharField(max_length=100, blank=True, null=True)
    facility_name = models.CharField(max_length=255)
    county = models.CharField(max_length=100)
    sub_county = models.CharField(max_length=100, blank=True, null=True)
    ward = models.CharField(max_length=100, blank=True, null=True)
    malaria_risk_zone = models.CharField(max_length=50, default='HIGH')
    pin_hash = models.CharField(max_length=255, blank=True, null=True)
    is_approved = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.full_name} ({self.profession} - {self.facility_name}) - Approved: {self.is_approved}"


class Patient(models.Model):
    """Pediatric patient profile model scoped per facility."""
    patient_uid = models.CharField(max_length=50, unique=True)
    full_name = models.CharField(max_length=255)
    date_of_birth = models.DateField()
    sex = models.CharField(max_length=10, choices=[('Male', 'Male'), ('Female', 'Female')])
    caregiver_name = models.CharField(max_length=255, blank=True, null=True)
    guardian_phone = models.CharField(max_length=20, blank=True, null=True)
    birth_certificate_number = models.CharField(max_length=100, blank=True, null=True)
    facility_name = models.CharField(max_length=255)
    county = models.CharField(max_length=100)
    risk_level = models.CharField(max_length=20, default='LOW')
    created_by = models.ForeignKey(UserProfile, on_delete=models.SET_NULL, null=True, blank=True, related_name='created_patients')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.full_name} ({self.patient_uid}) - {self.facility_name}"


class TriageSession(models.Model):
    """WHO IMCI Triage Assessment session tracking patient visits."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_name='triage_sessions')
    visit_number = models.IntegerField(default=1)
    danger_signs = models.TextField(blank=True, default='')
    temperature = models.FloatField(blank=True, null=True)
    respiratory_rate = models.IntegerField(blank=True, null=True)
    weight_kg = models.FloatField(blank=True, null=True)
    muac_mm = models.FloatField(blank=True, null=True)
    cough_classification = models.CharField(max_length=100, blank=True, null=True)
    diarrhea_classification = models.CharField(max_length=100, blank=True, null=True)
    fever_classification = models.CharField(max_length=100, blank=True, null=True)
    ear_classification = models.CharField(max_length=100, blank=True, null=True)
    nutrition_classification = models.CharField(max_length=100, blank=True, null=True)
    overall_risk = models.CharField(max_length=50, default='LOW')
    treatment_notes = models.TextField(blank=True, default='')
    created_by = models.ForeignKey(UserProfile, on_delete=models.SET_NULL, null=True, blank=True, related_name='triage_sessions')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Visit #{self.visit_number} for {self.patient.full_name} - Risk: {self.overall_risk} ({self.created_at.strftime('%Y-%m-%d')})"


class Vaccination(models.Model):
    """KEPI Immunization log model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_name='vaccinations')
    vaccine_name = models.CharField(max_length=100)
    target_disease = models.CharField(max_length=255)
    dose_number = models.IntegerField(default=1)
    is_given = models.BooleanField(default=False)
    given_date = models.DateField(blank=True, null=True)
    batch_number = models.CharField(max_length=100, blank=True, null=True)

    def __str__(self):
        return f"{self.vaccine_name} for {self.patient.full_name} - Given: {self.is_given}"


class ChatMessage(models.Model):
    """AfyaGPT AI Decision Support conversation history model."""
    patient = models.ForeignKey(Patient, on_delete=models.CASCADE, related_name='chat_messages', blank=True, null=True)
    sender = models.CharField(max_length=20, choices=[('USER', 'USER'), ('AI', 'AI')])
    message_text = models.TextField()
    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"[{self.sender}] {self.message_text[:30]}..."


class NewsArticle(models.Model):
    """News and updates model for the landing page."""
    title = models.CharField(max_length=255)
    slug = models.SlugField(unique=True)
    summary = models.TextField()
    content = models.TextField()
    category = models.CharField(max_length=100, default='Health Impact')
    author = models.CharField(max_length=100, default='AfyaGPT Team')
    image_url = models.URLField(blank=True, null=True)
    published_at = models.DateTimeField(auto_now_add=True)
    is_featured = models.BooleanField(default=False)

    class Meta:
        ordering = ['-published_at']

    def __str__(self):
        return self.title


class ContactInquiry(models.Model):
    """Contact form submissions from the landing page."""
    name = models.CharField(max_length=255)
    email = models.EmailField()
    phone = models.CharField(max_length=20, blank=True, null=True)
    subject = models.CharField(max_length=255)
    message = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    is_resolved = models.BooleanField(default=False)

    class Meta:
        ordering = ['-created_at']

    def __str__(self):
        return f"Inquiry from {self.name} - {self.subject}"


class Announcement(models.Model):
    """Ministry of Health & Facility Announcements broadcast to mobile CHWs."""
    title = models.CharField(max_length=255)
    message = models.TextField()
    priority = models.CharField(max_length=20, choices=[('INFO', 'INFO'), ('URGENT', 'URGENT')], default='INFO')
    target_county = models.CharField(max_length=100, default='ALL')
    created_at = models.DateTimeField(auto_now_add=True)
    is_active = models.BooleanField(default=True)

    class Meta:
        ordering = ['-created_at']

    def __str__(self):
        return f"[{self.priority}] {self.title}"


class AppSetting(models.Model):
    """Global system settings managed by assigned Stakeholder groups."""
    key = models.CharField(max_length=100, unique=True)
    value = models.TextField()
    description = models.CharField(max_length=255, blank=True, null=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.key} = {self.value}"
