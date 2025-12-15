import 'package:flutter/material.dart';
import '../models/course.dart';
import '../services/education_service.dart';

class CourseDetailPage extends StatefulWidget {
  final Course course;

  const CourseDetailPage({Key? key, required this.course}) : super(key: key);

  @override
  State<CourseDetailPage> createState() => _CourseDetailPageState();
}

class _CourseDetailPageState extends State<CourseDetailPage> {
  bool _isLoading = false;
  Course? _fullCourse;
  int? _selectedQuizAnswer;
  Map<int, int> _quizAnswers = {};

  @override
  void initState() {
    super.initState();
    _loadFullCourse();
  }

  Future<void> _loadFullCourse() async {
    if (widget.course.id == null) return;

    setState(() => _isLoading = true);
    try {
      final course = await EducationService.getCourse(widget.course.id!);
      setState(() {
        _fullCourse = course;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: $e')),
        );
      }
    }
  }

  Future<void> _startCourse() async {
    if (widget.course.id == null) return;

    setState(() => _isLoading = true);
    try {
      await EducationService.startCourse(widget.course.id!);
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Cours démarré avec succès !'),
            backgroundColor: Colors.green,
          ),
        );
        // Recharger les détails du cours pour mettre à jour la progression
        _loadFullCourse();
      }
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        String errorMessage = 'Erreur: $e';
        if (e.toString().contains('authentification') || e.toString().contains('401') || e.toString().contains('403')) {
          errorMessage = 'Erreur d\'authentification. Veuillez vous reconnecter.';
        }
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(errorMessage),
            backgroundColor: Colors.red,
            duration: const Duration(seconds: 5),
          ),
        );
      }
    }
  }

  Future<void> _completeCourse() async {
    if (widget.course.id == null) return;

    // Calculer le score basé sur les quiz
    int score = 0;
    if (_fullCourse?.quizzes != null) {
      int correct = 0;
      for (var quiz in _fullCourse!.quizzes!) {
        if (_quizAnswers[quiz.id] == quiz.correctAnswerIndex) {
          correct++;
        }
      }
      score = (_fullCourse!.quizzes!.isNotEmpty)
          ? (correct * 100 / _fullCourse!.quizzes!.length).round()
          : 0;
    }

    setState(() => _isLoading = true);
    try {
      await EducationService.completeCourse(widget.course.id!, score);
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Cours complété ! Score: $score%'),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context);
      }
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final course = _fullCourse ?? widget.course;

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: const Text('Détails du guide'),
        backgroundColor: const Color(0xFF4DD0E1),
      ),
      body: _isLoading && _fullCourse == null
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildCourseHeader(course),
                  const SizedBox(height: 24),
                  _buildCourseContent(course),
                  if (course.quizzes != null && course.quizzes!.isNotEmpty) ...[
                    const SizedBox(height: 24),
                    _buildQuizzesSection(course.quizzes!),
                  ],
                  const SizedBox(height: 24),
                  _buildActionButtons(course),
                ],
              ),
            ),
    );
  }

  Widget _buildCourseHeader(Course course) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF4DD0E1), Color(0xFF26C6DA)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            course.title,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            course.description,
            style: const TextStyle(
              color: Colors.white70,
              fontSize: 16,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              _buildHeaderBadge(course.category, Colors.white),
              const SizedBox(width: 8),
              _buildHeaderBadge(course.difficulty, Colors.white),
              if (course.durationMinutes != null) ...[
                const SizedBox(width: 8),
                _buildHeaderBadge('${course.durationMinutes} min', Colors.white),
              ],
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderBadge(String text, Color textColor) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.2),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: textColor,
          fontSize: 12,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Widget _buildCourseContent(Course course) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Contenu du guide',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            course.content,
            style: const TextStyle(
              fontSize: 16,
              height: 1.6,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuizzesSection(List<Quiz> quizzes) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Quiz',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),
          ...quizzes.asMap().entries.map((entry) {
            final index = entry.key;
            final quiz = entry.value;
            return _buildQuizCard(quiz, index);
          }),
        ],
      ),
    );
  }

  Widget _buildQuizCard(Quiz quiz, int index) {
    final selectedAnswer = _quizAnswers[quiz.id];
    final showResult = selectedAnswer != null;

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: showResult && selectedAnswer == quiz.correctAnswerIndex
              ? Colors.green
              : showResult && selectedAnswer != quiz.correctAnswerIndex
                  ? Colors.red
                  : Colors.grey[300]!,
          width: 2,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Question ${index + 1}',
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.bold,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            quiz.question,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 12),
          ...quiz.options.asMap().entries.map((entry) {
            final optionIndex = entry.key;
            final option = entry.value;
            final isSelected = selectedAnswer == optionIndex;
            final isCorrect = optionIndex == quiz.correctAnswerIndex;

            Color? backgroundColor;
            Color? textColor;
            IconData? icon;

            if (showResult) {
              if (isCorrect) {
                backgroundColor = Colors.green.withOpacity(0.1);
                textColor = Colors.green;
                icon = Icons.check_circle;
              } else if (isSelected && !isCorrect) {
                backgroundColor = Colors.red.withOpacity(0.1);
                textColor = Colors.red;
                icon = Icons.cancel;
              }
            } else if (isSelected) {
              backgroundColor = const Color(0xFF4DD0E1).withOpacity(0.1);
              textColor = const Color(0xFF4DD0E1);
            }

            return InkWell(
              onTap: showResult ? null : () {
                setState(() {
                  _quizAnswers[quiz.id!] = optionIndex;
                });
              },
              child: Container(
                margin: const EdgeInsets.only(bottom: 8),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: backgroundColor ?? Colors.white,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: isSelected
                        ? textColor ?? const Color(0xFF4DD0E1)
                        : Colors.grey[300]!,
                  ),
                ),
                child: Row(
                  children: [
                    if (icon != null)
                      Icon(icon, color: textColor, size: 20)
                    else
                      Container(
                        width: 20,
                        height: 20,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: isSelected
                                ? textColor ?? const Color(0xFF4DD0E1)
                                : Colors.grey[400]!,
                          ),
                          color: isSelected
                              ? textColor ?? const Color(0xFF4DD0E1)
                              : Colors.transparent,
                        ),
                        child: isSelected
                            ? const Icon(Icons.check, size: 14, color: Colors.white)
                            : null,
                      ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        option,
                        style: TextStyle(
                          color: textColor ?? Colors.black87,
                          fontWeight: isSelected ? FontWeight.w500 : FontWeight.normal,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          }),
          if (showResult && quiz.explanation != null) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  const Icon(Icons.info, color: Colors.blue, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      quiz.explanation!,
                      style: const TextStyle(
                        color: Colors.blue,
                        fontSize: 14,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildActionButtons(Course course) {
    final allQuizzesAnswered = course.quizzes != null &&
        course.quizzes!.isNotEmpty &&
        _quizAnswers.length == course.quizzes!.length;

    return Row(
      children: [
        Expanded(
          child: ElevatedButton.icon(
            onPressed: _isLoading ? null : _startCourse,
            icon: const Icon(Icons.play_arrow),
            label: const Text('Démarrer'),
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF4DD0E1),
              padding: const EdgeInsets.symmetric(vertical: 16),
            ),
          ),
        ),
        if (course.quizzes != null && course.quizzes!.isNotEmpty) ...[
          const SizedBox(width: 12),
          Expanded(
            child: ElevatedButton.icon(
              onPressed: _isLoading || !allQuizzesAnswered
                  ? null
                  : _completeCourse,
              icon: const Icon(Icons.check_circle),
              label: const Text('Terminer'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green,
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ),
        ],
      ],
    );
  }
}

